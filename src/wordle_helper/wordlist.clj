(ns wordle-helper.wordlist
  "Working with sets of possible words."
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [wordle-helper.helpers :as util]))

(defn read-word-list
  "Create a set of all valid WORDS from resources/${filename}.txt."
  [filename]
  (set (-> (.getFile (io/resource filename))
           slurp
           str/upper-case
           str/split-lines)))

(def popular-word-list (read-word-list "popular5.txt"))
(def huge-word-list (read-word-list "all5.txt"))

(comment
  (def feedback-kws {"1" :cold
                     "2" :warm
                     "3" :hot})
;; => #'wordle-helper.wordlist/feedback-kws

  (defn gf->list
    "Convert a gf to a sequence of pairs"
    [{:keys [guess feedback]}]
    (let [kws (map feedback-kws (str/split feedback #""))]
      (list* (mapv #(vector %1 %2) guess kws))))

  (gf->list {:guess "SOARE" :feedback "12113"})
;; => ([\S :cold] [\O :warm] [\A :cold] [\R :cold] [\E :hot])

  )


(defn word-works?
  "Is `word` consistent with the given guess & feedback (gf)?

   This question is the heart of the Wordle Helper: a guess & feedback allows
   you to dramatically reduce the space of possible words. At the beginning of
   the game, all words are possible. As you make guesses and receive feedback,
   the space of remaining-words is reduced until you've found the correct word!

   There are subtleties to correctly answering this question, especially when it
   comes to repeated letters. Let's consider the three types of feedback:

   * (l, 3): letter l appears in `word` in that position
   * (l, 2): letter l appears somewhere in `word` other than current position
   * (l, 1): letter l appears somewhere in `word` other than current position

  The correct way to do this is by reducing `word` each time!
  "
  [word {:keys [guess feedback]}]
  (loop [idx 0]
    (println word (class word))
    (if (= idx (count word))
      true
      (let [word-letter (util/nth-letter word idx)
            guess-letter (util/nth-letter guess idx)
            feedback-digit (util/nth-letter feedback idx)]
        (cond
          (and (= feedback-digit "3") (not (= word-letter guess-letter))) false
          (and (= feedback-digit "1") (str/includes? word guess-letter)) false
          (and (= feedback-digit "2")
               (or (= word-letter guess-letter)
                   (not (str/includes? word guess-letter)))) false
          :else (recur (inc idx)))))))

(comment
  ; It's tempting to implement word-works? at the character level. But when
  ; feedback-digit == 2, you need to check the whole word to make sure that
  ; the guess-letter appears somewhere in the word!
  (word-works? "CLING" {:guess "SOARE" :feedback "11213"})
;; => false
  (word-works? "HORSE" {:guess "SOARE" :feedback "23123"})
;; => true
  (word-works? "ASKEW" {:guess "SOARE" :feedback "21212"})
;; => true
  (word-works? "ASKEW" {:guess "DITCH" :feedback "11111"})
;; => true
  (word-works? "ASKEW" {:guess "PLANK" :feedback "11212"})
;; => true
  (word-works? "ASKEW" {:guess "SWIMS" :feedback "22111"});; => false
)

(defn filter-using-gf
  "Find the subset of words that are consistent with guess-with-feedback gf."
  [gf words]
  (set (filter #(word-works? % gf) words)))

(defn filter-using-gfs
  "Find the `words` that are consistent with all `gfs`, applied sequentially."
  [gfs words]
  (reduce filter-using-gf gfs words))

(defn most-common-letters
  "Get the n most common letters in `words`, including repeated letters."
  [words n]
  (->> (map #(str/split % #"") words)
       flatten
       frequencies
       (sort-by val >)
       (take n)))
