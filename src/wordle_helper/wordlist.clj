(ns wordle-helper.wordlist
  "Starting with, and filtering, sets of possible words."
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [wordle-helper.utils :as util]))

(defn read-word-list
  "Create a set of all valid WORDS from resources/${filename}.txt."
  [filename]
  (set (-> (.getFile (io/resource filename))
           slurp
           str/upper-case
           str/split-lines)))

(def popular-word-list (read-word-list "popular5.txt"))
(def huge-word-list (read-word-list "all5.txt"))

;; For each letter in your guess, Wordle will tell you one of three things:
;; - Hot: the letter you guessed appears in the answer in exactly the position
;;        in which you guessed it
;; - Warm: the letter you guessed appears somewhere other than the position in
;;         which you guessed it
;; - Cold: the letter you guessed doesn't appear in the answer, after taking
;;         your Warm and Hot letters into consideration
;;
;; Wordle Helper uses this feedback to reduce a larger set of words into the
;; smaller set of words consistent with this feedback.
;;
;; Most people probably assume that Cold means "letter doesn't appear". I
;; certaintly did. But that's incorrectly. If you guess SWIMS and the answer is
;; ASKEW, then your first S will be Warm (there's an S somewhere other than the
;; first position) and your second S will be Cold (you've already been told
;; about all the S's in the word).
;;
;; The trick to interpreting a guess & feedback correctly is to do it in stages:
;; 1. Make sure all :hot guess letters appear in exactly the same positions in
;;    the word. I
;; 2. After removing all :hot letters from the word, all :warm guess letters
;;    appear somewhere in the word other than their guessed position
;; 3. After removing all :hot letters and :warm letters from the word, all :cold
;;    letters are absent from the word.
;; But instead of "removing" letters, we'll just replace them with a "_" so that
;; indices are preserved.

(def feedback-kws
  {"1" :cold
   "2" :warm
   "3" :hot})

(defn gf->tempmap
  "Convert a gf into a map: :temp -> [{:index :letter :temp}, ...]."
  [{:keys [guess feedback]}]
  (let [letters (str/split guess #"")
        temps (map feedback-kws (str/split feedback #""))
        triplets (map #(hash-map :index %1 :guess-letter %2 :temp %3)
                      (range) letters temps)]
    (group-by :temp triplets)))

(comment
  (gf->tempmap {:guess "SOARE" :feedback "12113"})
;; => {:cold [{:temp :cold, :index 0, :guess-letter "S"}
;;            {:temp :cold, :index 2, :guess-letter "A"}
;;            {:temp :cold, :index 3, :guess-letter "R"}],
;;     :warm [{:temp :warm, :index 1, :guess-letter "O"}],
;;     :hot [{:temp :hot, :index 4, :guess-letter "E"}]}
  )

(defn hot-reducer
  "Replace a correct `hot` letter in word-letters with '_', or return nil if
   the corresponding letter in `word-letters` doesn't match the `hot` letter"
  [word-letters hot]
  (when (some? word-letters)
    (let [{:keys [guess-letter index]} hot
          word-letter (get word-letters index)]
      (if (not= guess-letter word-letter)
        nil
        (assoc word-letters index "_")))))

(defn word-letters-after-hots
  "Prototype."
  [word-letters hots]
  (reduce hot-reducer word-letters hots))

(comment
  (def word-letters (str/split "STARK" #""))
  (word-letters-after-hots word-letters [])
;; => ["S" "T" "A" "R" "K"]
  (word-letters-after-hots word-letters [{:temp :hot, :index 0, :letter "S"}
                                         {:temp :hot, :index 2, :letter "A"}])
;; => ["_" "T" "_" "R" "K"]
  (word-letters-after-hots word-letters [{:temp :hot, :index 0, :letter "S"}
                                         {:temp :hot, :index 2, :letter "X"}])
;; => nil
  )

(defn warm-reducer
  "Warm reduce."
  [word-letters warm]
  (when (some? word-letters)
    (let [{:keys [guess-letter index]} warm
          word-letter (get word-letters index)]
      (cond (= guess-letter word-letter) nil
            (not (util/vector-contains? word-letters guess-letter)) nil
            :else (let [match-index (.indexOf word-letters guess-letter)]
                    (assoc word-letters match-index "_"))))))

(defn word-letters-after-warms
  "Prototype."
  [word-letters warms]
  (reduce warm-reducer word-letters warms))

(defn cold-reducer
  "Cold reduce."
  [word-letters cold]
  (when (some? word-letters)
    (let [{:keys [guess-letter]} cold]
      (if (util/vector-contains? word-letters guess-letter)
        nil
        word-letters))))

(defn word-letters-after-colds
  "Prototype."
  [word-letters colds]
  (reduce cold-reducer word-letters colds))

(defn word-works?
  "Is `word` consistent with the guess & feedback provided?"
  [word gf]
  (let [word-letters (str/split word #"")
        tempmap (gf->tempmap gf)]
    (-> word-letters
         (word-letters-after-hots (get tempmap :hot []))
         (word-letters-after-warms (get tempmap :warm []))
         (word-letters-after-colds (get tempmap :cold {})))))

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
