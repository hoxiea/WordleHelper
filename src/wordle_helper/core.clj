(ns wordle-helper.core
  "Main entry point for Wordle Helper CLI."
  (:gen-class)
  (:require
   [wordle-helper.config :as config :refer [game-options display-options]]
   [wordle-helper.guess-and-feedback :as gaf]
   [wordle-helper.utils :as util]
   [wordle-helper.printer :as wpr]
   [wordle-helper.best-word :as best]
   [wordle-helper.wordlist :as wordlist]))

;; TODO: use current value instead of initial value!
(def main-choices
  {:s "Print current status"
   :c (str "Toggle high-contrast mode (current: "
           (:high-contrast-mode? @game-options) ")")
   :g "Enter your next guess"
   :a "List all possible words"
   :l "Most common letters in remaining words"
   :b "Best words to guess"
   :w "Score a word you're considering guessing"
   :u "Undo your most recent guess"
   :q "Quit Wordle Helper"})

;; TODO: fix undo; find a way to test undo
(defn -main
  "Release the Wordle Helper!"
  []
  (println "Let's play Wordle!")
  (loop [gfs []
         remaining-words wordlist/word-list]

    (when (= (count gfs) (:num-guesses config/params))
      (println "Game over!"))
    (when (< (count remaining-words) 5)
      (util/print-sorted remaining-words))

    (let [choice (util/get-user-choice main-choices)]
      (case choice
        :s (do (wpr/print-game-status gfs remaining-words true)
               (recur gfs remaining-words))

        :c (do (config/toggle-high-contrast-mode!)
               (recur gfs remaining-words))

        :a (do (util/print-sorted remaining-words)
               (recur gfs remaining-words))

        :l (do (wpr/print-letter-freqs
                (wordlist/most-common-letters remaining-words
                                              (:num-top-letters display-options)))
               (recur gfs remaining-words))

        ;; TODO: cache letter scores for current remaining-words, then use for functions?
        ;;       could make a word-score function, for example
        :w (let [word (gaf/get-valid-guess-from-user
                       "What word would you like to score?")
                 word->score (best/guess-scores [word] remaining-words)
                 score (first (vals word->score))]
             (println (wpr/format-word-and-score word score) "\n")
             (recur gfs remaining-words))

        :b (let [valid-guesses (if (@game-options :hard-mode?)
                                 remaining-words
                                 wordlist/word-list)
                 scores (best/guess-scores valid-guesses remaining-words)
                 best-guesses (util/n-largest-vals scores (:num-best-words display-options))]
             (doseq [[word score] best-guesses]
               (println (wpr/format-word-and-score word score)))
             (println)
             (recur gfs remaining-words))

        :g (let [gf (gaf/get-user-guess-and-feedback)
                 new-remaining-words (wordlist/filter-using-gf remaining-words gf)]
             (if (empty? new-remaining-words)
               (do
                 (println (str "There are 0 words consistent with "
                               (wpr/format-gf gf) "!\n"
                               "Make sure you entered your info correctly."))
                 (recur gfs remaining-words))

               (let [updated-gfs (conj gfs gf)]
                 (println "Registered" (wpr/format-gf gf))
                 (wpr/print-game-status updated-gfs new-remaining-words true)
                 (recur updated-gfs new-remaining-words))))

        :u (if (empty? gfs)
             (recur gfs remaining-words) 

            (let [all-but-last-gf (pop gfs)
                    new-remaining-words (wordlist/filter-using-gfs wordlist/word-list 
                                                                   all-but-last-gf)]
             (recur all-but-last-gf new-remaining-words)))

        :q (do (println "Thanks for playing!")
               (System/exit 0))

        (do (println "Choice" choice "not recognized! Please try again.\n")
            (recur gfs remaining-words))))))
