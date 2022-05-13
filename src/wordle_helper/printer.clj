(ns wordle-helper.printer
  "Functions related to printing text to the terminal for CLI version."
  (:require
   [clojure.term.colors :as color]
   [wordle-helper.config :as config :refer [game-options]]))

;; FEEDBACK INFORMATION
;; :cold - letter doesn't appear in word, given earlier feedback
;; :warm - letter appears in word, but not in guessed position
;; :hot  - letter appears in word in guessed position!

#_:clj-kondo/ignore
(def text-color color/white)

#_:clj-kondo/ignore
(defn bg-color
  "Get the background color function for the text."
  [temp]
  (case temp
    :cold color/on-grey
    :warm (if (:high-contrast-mode? @game-options)
            color/on-blue
            color/on-yellow)
    :hot (if (:high-contrast-mode? @game-options)
           color/on-red
           color/on-green)))

(defn render-pair
  "Render a letter + temp for printing, e.g. ['S', :hot]"
  [[letter temp]]
  ((bg-color temp) (text-color letter)))

(comment
  (render-pair ["S", :hot])  ;; => "[41m[37mS[0m[0m"
  (render-pair ["O", :warm]) ;; => "[44m[37mO[0m[0m"
  )

(defn format-gf [gf]
  (apply str (map render-pair gf)))

(comment
  ;; For each pair, you get character codes that set the background & text color
  (format-gf '(["S" :cold] ["O" :warm] ["A" :cold]))
  ;; => "[40m[37mS[0m[0m[44m[37mO[0m[0m[40m[37mA[0m[0m"
  ;; letters:       ^                    ^                    ^
  )


(defn print-game-status
  "Print the current Wordle Helper status."
  ([guesses remaining-words show-num-words?]
   (if (some? guesses)
     (doseq [guess-string (map format-gf guesses)]
       (println guess-string))
     (println "No guesses yet!"))
   (when show-num-words?
     (println (count remaining-words) "possible words remain!\n")))
  ([guesses remaining-words] (print-game-status guesses remaining-words false)))

(defn print-letter-freqs
  "Print nicely-formatted letter frequencies."
  [letter-freqs]
  (doseq [[letter count] letter-freqs]
    (println (str letter ": " count)))
  (println))

(defn format-word-and-score
  "Make a nicely-formatted string out of a word and its information score."
  [word score]
  (str word ": " (format "%.1f" (* (float score) 100))))
