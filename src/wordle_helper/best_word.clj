(ns wordle-helper.best-word
  "Finding the best guess for you!"
  (:require
   [clojure.string :as str]
   ))

;;; wordle-helper.best-word
;;; Finding the most informative guess from a set of possible words, based on
;;; the feedback Wordle has already given you.

;;; KEY IDEA: A letter that's in ~half the possible words will be a good letter
;;; to guess! In contrast, a letter that's present in all possible words or no
;;; possible words is totally uninformative. This is nicely captured via the
;;; following info-score:
(defn info-score
  "Compute an information score := p*(1-p), where p = count/total."
  [count total]
  {:pre [(pos-int? total)
         (>= count 0)
         (<= count total)]}
  (let [p (/ count total)]
    (* p (- 1 p))))

;;; Next, for a given set of words, compute an info-score for all 26 letters.
(def cap-letters
  (let [[A Z] (map int "AZ")
        ascii-codes (range A (inc Z))]
    (map #(str (char %)) ascii-codes)))
; ("A" "B" ... "Z")

(defn letter-counts
  "In how many `words` does each cap-letter occur at least once?"
  [words]
  (letfn [(num-words-containing-letter [letter]
           (count (filter #(str/includes? % letter) words)))]
    (zipmap cap-letters
            (map num-words-containing-letter cap-letters))))

(defn letter-scores
  "Map each capital letter to its info-score, based on the words in `words`."
  [words]
  (let [num-words (count words)]
    (into {} (for [[letter count] (letter-counts words)]
               [letter (info-score count num-words)]))))

;;; But we can't just guess the 5 most informative letters (for our set of
;;; remaining words) because Wordle only allows us to guess real words.
;;; Thankfully, at any point in the game, we have a good set of valid-guesses to
;;; search for the most informative guesses:
;;; - If not hard-mode, valid-guesses == master-word-list
;;; - If hard-mode, valid-guesses == remaining-words
;;;
;;; Either way, we need to compute scores for each valid-guess, based on the
;;; letter frequences in remaining-words. (A word's score is just the sum of its
;;; unique letters' info-scores.)
(defn guess-scores
  "Compute the info score for each guess in valid-guesses to narrow down
   remaining-words."
  [valid-guesses remaining-words]
  (let [lscores (letter-scores remaining-words)]
    (letfn [(sum [xs] (reduce + xs))
            (unique-letters [s] (map str (distinct s)))
            (word-score [word] (sum (map lscores (unique-letters word))))]
      (into {} (for [word valid-guesses]
                 [word (word-score word)])))))

(defn n-largest-vals
  "Get the `n` largest key-value pairs from map `m`."
  [m n]
  (->> m (sort-by val >) (take n)))
