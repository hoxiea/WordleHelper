(ns wordle-helper.wordlist
  "Working with sets of possible words."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [wordle-helper.guess-and-feedback :as gaf]))

(defn create-word-list
  "Create a set of all valid words from resources/popular5.txt."
  []
  (set (->
        (.getFile (io/resource "popular5.txt"))
        slurp
        str/upper-case
        str/split-lines)))

(def master-word-list (create-word-list))

(defn filter-using-gf
  "Find the subset of words that are consistent with guess-with-feedback gf."
  [words gf]
  (filter #(gaf/word-works? % gf) words))

(defn filter-using-gfs
  "Find the subset of words that are consistent with the gfs.
   If no words are provided, start with the master-word-list."
  ([words gfs]
   (reduce filter-using-gf words gfs))
  ([gfs]
   (filter-using-gfs master-word-list gfs)))

(defn most-common-letters
  "Get the n most common letters in words."
  [words n]
  (->> (map #(str/split % #"") words)
       flatten
       frequencies
       (sort-by val >)
       (take n)))

;; ("A" "B" ... "Z")
(def cap-letters 
  (let [[A Z] (map int "AZ")
        ascii-codes (range A (inc Z))]
    (map #(str (char %)) ascii-codes)))

(defn num-words-containing-letter
  "How many words contain the given letter?"
  [words letter]
  (count (filter #(str/includes? % letter) words)))

(defn letter-counts
  "In how many words does each cap-letter occur at least once?"
  [words]
  (zipmap cap-letters
          (map #(num-words-containing-letter words %) cap-letters)))

(defn freq-score
  "Compute an information score := p*(1-p), where p = count/total."
  [count total]
  (assert (pos-int? total) "total must be positive!")
  (assert (<= count total) "count must be less than or equal to total!")
  (assert (>= count 0) "count must be non-negative!")
  (let [p (/ count total)]
    (* p (- 1 p))))

(defn letter-scores
  "Map each cap-letter to its freq-score, based on the words in `words`."
  [words]
  (let [num-words (count words)]
    (into {} (for [[letter count] (letter-counts words)]
               [letter (freq-score count num-words)]))))

(defn word-score
  "How informative is a guess of `word` expected to be? Sum of its distinct letters' scores,
   where lscores were likely computed from a set of all remaining words."
  [word lscores]
  (let [disctinct-letters (map str (distinct word))
        word-letter-scores (map #(get lscores %) disctinct-letters)]
    (reduce + word-letter-scores)))

(defn master-list-scores
  "Compute the word-score for each word in the master list, where the lscores are computed using
   the words in remaining words. Will give us our most informative guesses!"
  [remaining-words]
  (let [lscores (letter-scores remaining-words)]
    (into {} (for [word master-word-list]
               [word (word-score word lscores)]))))

(defn most-informative-guesses
  "Which n words from the master list have the highest word-scores, based on the current remaining-words?"
  [remaining-words n]
  (->> remaining-words
       master-list-scores
       (sort-by val >)
       (take n)))
  