(ns wordle-helper.core
  "Main entry point for Wordle Helper."
  (:gen-class)
  (:require
   [clojure.string :as str]
   [wordle-helper.config :as config :refer [options]]
   [wordle-helper.guess-and-feedback :as gaf]
   [wordle-helper.helpers :as util]
   [wordle-helper.printer :as wpr]
   [wordle-helper.best-word :as best]
   [wordle-helper.wordlist :as wordlist]))


(def main-choices
  {:s "Print current status"
   :g "Enter your next guess"
   :a "List all possible words"
   :l "Most common letters in remaining words"
   :b "Best words to guess"
   :u "Undo your most recent guess"
   :q "Quit Wordle Helper"})


;; TODO: let the user specify a count for :l, :b
;; TODO: add :w for a single word score breakdown

(defn get-user-choice
  "Get a valid choice from the map of choices via *in*."
  [choices]
  (let [valid-options (set (map name (keys choices)))]
    (loop []
      (println "What would you like to do?")
      (doseq [[choice descr] choices]
        (println (str (name choice) ": " descr)))

      (let [in (-> (read-line) str/trim str/lower-case)]
        (cond
          (= in "")
          (do (println "Please select an option!\n")
              (recur))

          (contains? valid-options (util/first-letter in))
          (keyword (util/first-letter in))

          :else
          (do (println "\nInvalid option - please try again!\n")
              (recur)))))))


(defn print-status
  "Print the current Wordle Helper status."
  ([guesses remaining-words show-num-words?]
   (if (empty? guesses)
     (println "No guesses yet!")
     (doseq [guess-string (map wpr/format-gf guesses)]
       (println guess-string)))
   (when show-num-words?
     (println (count remaining-words) "possible words remain!\n")))
  ([guesses remaining-words] (print-status guesses remaining-words false)))


(defn -main
  "Release the Wordle Helper!"
  []
  (println "Let's play Wordle!")

  (loop [gfs []
         remaining-words wordlist/popular-word-list]

    (when (= (count gfs) (options :num-guesses))
      (println "Game over!"))
    (when (< (count remaining-words) 5)
      (util/print-sorted remaining-words))

    (let [choice (get-user-choice main-choices)]
      (case choice
        :s (do (print-status gfs remaining-words true)
               (recur gfs remaining-words))

        :a (do (util/print-sorted remaining-words)
               (recur gfs remaining-words))

        :l (let [letter-freqs (wordlist/most-common-letters remaining-words 10)]
             (doseq [[letter count] letter-freqs]
               (println (str letter ": " count)))
             (println)
             (recur gfs remaining-words))

        :b (let [valid-guesses (if (options :hard-mode?)
                                 remaining-words
                                 wordlist/popular-word-list)
                 scores (best/guess-scores valid-guesses remaining-words)
                 best-guesses (best/n-largest-vals scores 5)]
             (doseq [[word score] best-guesses]
               (println (str word ": " (format "%.1f" (* (float score) 100)))))
             (println)
             (recur gfs remaining-words))

        :g (let [gf (gaf/get-user-guess-and-feedback)
                 new-remaining-words (wordlist/filter-using-gf gf remaining-words)]
             (if (empty? new-remaining-words)
               (if (gaf/gf-confirmed? gf)
                 (recur gfs (wordlist/filter-using-gfs (conj gfs gf)
                                                       wordlist/huge-word-list))
                 (recur gfs remaining-words))
               (let [updated-gfs (conj gfs gf)]
                 (println "Registered" (wpr/format-gf gf))
                 (print-status updated-gfs new-remaining-words true)
                 (recur updated-gfs new-remaining-words))))

        :u (let [all-but-last-gf (pop gfs)
                 new-remaining-words (wordlist/filter-using-gfs all-but-last-gf
                                                                wordlist/popular-word-list)]
             (recur all-but-last-gf new-remaining-words))

        :q (do (println "Thanks for playing!")
               (System/exit 0))

        (do (println "Choice" choice "not recognized! Please try again.\n")
            (recur gfs remaining-words))))))
