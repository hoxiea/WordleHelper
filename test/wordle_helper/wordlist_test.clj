(ns wordle-helper.wordlist-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [wordle-helper.wordlist :as sut]))

(deftest tempmaps
  (let [gf {:guess "SOARE" :feedback "31221"}
        tempmap (sut/gf->tempmap gf)]
    (testing "we get the correct number of cold, warm, and hot entries"
      (is (= 1 (count (:hot tempmap))))
      (is (= 2 (count (:warm tempmap))))
      (is (= 2 (count (:cold tempmap)))))
    (testing "the cold indices are correct"
      (let [cold-indices (set (map :index (:cold tempmap)))]
        (is (= #{1 4} cold-indices))))
    (testing "the warm indices are correct"
      (let [warm-indices (set (map :index (:warm tempmap)))]
        (is (= #{2 3} warm-indices))))
)) ; tempmaps


(deftest word-letters-after-hots
  (testing "changes nothing if no :hot letters"
    (let [hots []
          word-letters (str/split "ABCDE" #"")
          expected word-letters]
      (is (= expected (sut/word-letters-after-hots word-letters hots)))))

  (testing "replaces all matching :hots with underscores"
    (let [gf {:guess "SOARE" :feedback "31331"}
          hots (:hot (sut/gf->tempmap gf))
          word-letters (str/split "STARS" #"")
          expected ["_" "T" "_" "_" "S"]]
      (is (= expected (sut/word-letters-after-hots word-letters hots)))))

  (testing "if any :hots mismatch, then letters-after-hots returns nil"
    (let [gf {:guess "SOARE" :feedback "31331"}
          hots (:hot (sut/gf->tempmap gf))
          word-letters (str/split "STAMP" #"")  ; M != R
          expected nil]
      (is (= expected (sut/word-letters-after-hots word-letters hots)))))
)  ;; letters-after-hots

(deftest letters-after-warms
  (testing "passes a nil word-letters along as nil"
    (let [gf {:guess "SOARE" :feedback "21111"}
          warms (:warm (sut/gf->tempmap gf))
          word-letters nil
          expected nil]
      (is (= expected (sut/word-letters-after-warms word-letters warms)))))

  (testing "returns nil if any :warms match perfectly"
    (let [gf {:guess "SOARE" :feedback "21111"}
          warms (:warm (sut/gf->tempmap gf))
          word-letters (str/split "STAMP" #"")  ; first letter cannot be S
          expected nil]
      (is (= expected (sut/word-letters-after-warms word-letters warms)))))

  (testing "returns nil if a :warm guess letter isn't in the word"
    (let [gf {:guess "SOARE" :feedback "21111"}
          warms (:warm (sut/gf->tempmap gf))
          word-letters (str/split "ABCDE" #"")  ; no S anywhere
          expected nil]
      (is (= expected (sut/word-letters-after-warms word-letters warms)))))

  (testing "replaces a matching letter with an underscore"
    (let [gf {:guess "SOARE" :feedback "21111"}
          warms (:warm (sut/gf->tempmap gf))
          word-letters (str/split "XXXXS" #"")
          expected ["X" "X" "X" "X" "_"]]
      (is (= expected (sut/word-letters-after-warms word-letters warms)))))

  (testing "replaces the first matching letter with an underscore if more than 1"
    (let [gf {:guess "SOARE" :feedback "21111"}
          warms (:warm (sut/gf->tempmap gf))
          word-letters (str/split "X_XSS" #"")
          expected ["X" "_" "X" "_" "S"]]
      (is (= expected (sut/word-letters-after-warms word-letters warms)))))
);; letters-after-warms

(deftest letters-after-colds
  (testing "passes a nil word-letters along as nil"
    (let [gf {:guess "SOARE" :feedback "31221"}
          colds (:cold (sut/gf->tempmap gf))
          word-letters nil
          expected nil]
      (is (= expected (sut/word-letters-after-colds word-letters colds)))))

  (testing "returns nil if the word contains a :cold letter"
    (let [gf {:guess "SOARE" :feedback "11111"}  ; no A, B, C, D, or E
          colds (:cold (sut/gf->tempmap gf))
          word "ABCDE"
          word-letters (str/split word #"")
          expected nil]
      (is (= expected (sut/word-letters-after-colds word-letters colds)))))

  (testing "passes along word-letters if it doesn't contain any cold letters"
    (let [gf {:guess "SOARE" :feedback "11111"}  ; no A, B, C, D, or E
          colds (:cold (sut/gf->tempmap gf))
          word "_WYXZ"
          word-letters (str/split word #"")
          expected word-letters]
      (is (= expected (sut/word-letters-after-colds word-letters colds)))))
)  ;; letters-after-colds

(deftest word-works
  (testing "word-works? correctly removes words that are inconsistent with a gf"
    (let [gf {:guess "SOARE" :feedback "11321"}
          ok-words ["BRAID" "CRAMP" "GRAIL" "TRAIN" "WRATH"]
          bad-words ["BORED" "LISPS" "TUPLE" "APPLE" "HORSE"]
          all-words (set (flatten [ok-words bad-words]))]

      (is (= (count ok-words)
             (count (sut/filter-using-gf all-words gf)))))))

(deftest filter-using-gfs
  (testing "ASKEW bug from 2022-04-25"
    (let [gfs [{:guess "SOARE" :feedback "21212"},
               {:guess "DITCH" :feedback "11111"},
               {:guess "PLANK" :feedback "11212"}]
          remaining-words (sut/filter-using-gfs sut/popular-word-list gfs)
          swims {:guess "SWIMS" :feedback "22111"}
          words-after-swims (sut/filter-using-gf remaining-words swims)
          ]
      (doseq [gf gfs]
        (is (seq (sut/word-works? "ASKEW" gf))))
      ;; Make sure that ASKEW remains after applying all gfs
      (is (true? (contains? remaining-words "ASKEW")))
      ;; ASKEW should still remain after applying SWIMS
        (is (true? (contains? words-after-swims "ASKEW")))
      )))
