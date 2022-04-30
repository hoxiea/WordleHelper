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

(def feedback-kws {"1" :cold
                   "2" :warm
                   "3" :hot})

(comment
  ;; TODO: delete, no longer used!
(defn gf->list
  "Convert a gf to a list of pairs that can be recursively processed."
  [{:keys [guess feedback]}]
  (let [kws (map feedback-kws (str/split feedback #""))]
    (list* (mapv #(vector (str %1) %2) guess kws))))

  (gf->list {:guess "SOARE" :feedback "12113"})
  ;; => (["S" :cold] ["O" :warm] ["A" :cold] ["R" :cold] ["E" :hot])
)

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
;; => {:cold [{:temp :cold, :index 0, :guess-letter "S"} {:temp :cold, :index 2, :guess-letter "A"} {:temp :cold, :index 3, :guess-letter "R"}], :warm [{:temp :warm, :index 1, :guess-letter "O"}], :hot [{:temp :hot, :index 4, :guess-letter "E"}]}
  )

(defn hot-reducer
  "Hot reduce."
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

(comment
  (word-letters-after-hots (str/split "STARK" #"") [])
;; => ["S" "T" "A" "R" "K"]
  (word-letters-after-hots (str/split "STARK" #"") [{:temp :hot, :index 0, :letter "S"}])
;; => ["_" "T" "A" "R" "K"]
  (word-letters-after-hots (str/split "STARK" #"") [{:temp :hot, :index 0, :letter "S"}
                                                    {:temp :hot, :index 2, :letter "A"}])
;; => ["_" "T" "_" "R" "K"]
  (word-letters-after-hots (str/split "STARK" #"") [{:temp :hot, :index 0, :letter "S"}
                                                    {:temp :hot, :index 2, :letter "X"}])
;; => nil
  )

(defn word-works?
  "Is `word` consistent with the guess & feedback provided?"
  [word gf]
  (let [word-letters (str/split word #"")
        tempmap (gf->tempmap gf)]
    (-> word-letters
         (word-letters-after-hots (get tempmap :hot []))
         (word-letters-after-warms (get tempmap :warm []))
         (word-letters-after-colds (get tempmap :cold {})))))

(comment
  ;; TODO: delete, no longer needed?
  (defn letters-after-hots
    "Apply the gfl :hot letters to word-letters, replacing matching letters with '_'
  or returning nil if there's a :hot letter in gfl that's not in word-letters."
    [gfl word-letters]
    (when (nil? word-letters) nil)
    (letfn [(replace-or-nil-hot [[gl temp] wl]
              (cond (not= temp :hot) wl
                    (= gl wl) "_"
                    :else nil))]
      (let [results (map replace-or-nil-hot gfl word-letters)]
        (if (some nil? results)
          nil
          results))))

  (defn extract-temp
    [temp gfl]
    (let [with-indices (zipmap (range) gfl)
          matches (filter #(= temp (second (second %))) with-indices)]
      (into {} (map (fn [[index [letter _]]] [index letter]) matches))))

  (extract-temp :hot  (gf->list {:guess "SOARE" :feedback "12113"}))
;; => {4 "E"}
  (extract-temp :warm  (gf->list {:guess "SOARE" :feedback "12113"}))
;; => {1 "O"}
  (extract-temp :cold  (gf->list {:guess "SOARE" :feedback "12113"}))
;; => {0 "S", 2 "A", 3 "R"}

  (defn letters-after-warms
    "Apply the gfl :warm letters to word-letters."
    [gfl word-letters]
    (let [with-indices (zipmap (range) gfl)
          warms (filter #(= :warm (second (second %))) with-indices)
          warms (into {} (map (fn [[index [letter _]]] [index letter]) warms))]
      (reduce update-word-letters-warms word-letters warms)))

  (defn letters-after-warms
    "Apply the gfl :warm letters to word-letters."
    [gfl word-letters]
    (cond
      (nil? word-letters) nil
      (empty? gfl) word-letters
      :else
      (let [[guess-letter temp] (first gfl)
            gfl-index (- 5 (count gfl))]
        (if (not= temp :warm)
          (letters-after-warms (rest gfl) word-letters)
          (let [gl-index (.indexOf word-letters guess-letter)]
            (cond (= -1 gl-index) nil
                  (= guess-letter (get word-letters gfl-index)) nil
                  :else
                  (letters-after-warms (rest gfl)
                                       (assoc word-letters gl-index "_"))))))))

  (defn letters-after-colds
    "Apply the gfl :cold letters to word-letters - returns nil if word-letters is
  nil or contains a :cold guess letter, otherwise returns word-letters"
    [gfl word-letters]
    (cond
      (nil? word-letters) nil
      (empty? gfl) word-letters
      :else
      (let [[gl temp] (first gfl)]
        (if (and (= temp :cold)
                 (not= -1 (.indexOf word-letters gl)))
          nil
          (letters-after-colds (rest gfl) word-letters)))))

  (defn word-works?
    "Is `word` consistent with the guess & feedback provided?"
    [word gf]
    (let [letters (str/split word #"")
          gfl (gf->list gf)]
      (->> letters
           (letters-after-hots gfl)
           (letters-after-warms gfl)
           (letters-after-colds gfl)))))


(comment
;; Old implementations of work-works?
(defn word-works?
  "Is `word` consistent with the given guess & feedback (gf)?

   This question is the heart of the Wordle Helper: a guess & feedback allows
   you to dramatically reduce the space of possible words. At the beginning of
   the game, all words are possible. As you make guesses and receive feedback,
   the space of remaining-words is reduced until you've found the correct word!

  Feedback can be interpreted from left to right."
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

)

(comment
  ; It's tempting to implement word-works? at the character level. But when
  ; feedback-digit == 2, you need to check the whole word to make sure that
  ; the guess-letter appears somewhere in the word!
  (word-works? "CLING" {:guess "SOARE" :feedback "11213"})
;; => nil
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
;; => #'wordle-helper.wordlist/most-common-letters
;; => #'wordle-helper.wordlist/most-common-letters
