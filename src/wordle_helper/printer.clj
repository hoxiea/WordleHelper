(ns wordle-helper.printer
  "Functions related to printing text to the terminal."
  (:require
   [clojure.term.colors :as color]
   [wordle-helper.utils :as util]))

(def text-color color/white)

;; FEEDBACK INFORMATION
;; "1" - letter doesn't appear in word
;; "2" - letter appears in word, but not in guessed position
;; "3" - letter appears in word, in guessed position
(def color-fns
  {"1" #(text-color %)
   "2" #(color/on-yellow (text-color %))
   "3" #(color/on-green  (text-color %))})
(defn cold [text] ((get color-fns "1") text))
(defn warm [text] ((get color-fns "2") text))
(defn hot  [text] ((get color-fns "3") text))

;; HELPER FUNCTIONS: FORMATTING GUESS+FEEDBACK
(defn apply-color
  [s feedback-str]
  ((get color-fns feedback-str) s))

(defn format-gf
  "Format a guess (using feedback) for printing to terminal."
  [{:keys [guess feedback]}]
  (loop [g guess
         f feedback
         current ""]
    (if (= g "")
      current
      (recur (subs g 1)
             (subs f 1)
             (str current (apply-color (util/first-letter g)
                                       (util/first-letter f)))))))

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

(defn format-word-and-score
  "Make a nicely-formatted string out of a word and its information score."
  [word score]
  (str word ": " (format "%.1f" (* (float score) 100))))
