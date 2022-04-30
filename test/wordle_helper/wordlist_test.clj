(ns wordle-helper.wordlist-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [wordle-helper.wordlist :as sut]))

(deftest letters-after-hots
  (testing "letters-after-hots changes nothing if no :hot letters"
    (let [gf {:guess "SOARE" :feedback "11221"}
          gfl (sut/gf->list gf)
          word-letters (str/split "ABCDE" #"")
          expected word-letters]
      (is (= expected (sut/letters-after-hots gfl word-letters)))))

  (testing "letters-after-hots replaces all matching :hots with underscores"
    (let [gf {:guess "SOARE" :feedback "31331"}
          gfl (sut/gf->list gf)
          word-letters (str/split "STARS" #"")
          expected ["_" "T" "_" "_" "S"]]
      (is (= expected (sut/letters-after-hots gfl word-letters)))))

  (testing "if any :hots mismatch, then letters-after-hots returns nil"
    (let [gf {:guess "SOARE" :feedback "31331"}
          gfl (sut/gf->list gf)
          word-letters (str/split "STAMP" #"")  ; M != R
          expected nil]
      (is (= expected (sut/letters-after-hots gfl word-letters)))))
)  ;; letters-after-hots


(deftest letters-after-warms
  (testing "passes a nil word-letters along as nil"
    (let [gf {:guess "SOARE" :feedback "21111"}
          gfl (sut/gf->list gf)
          word-letters nil
          expected nil]
      (is (= expected (sut/letters-after-warms gfl word-letters)))))

  (testing "returns nil if any :warms match perfectly"
    (let [gf {:guess "SOARE" :feedback "21111"}
          gfl (sut/gf->list gf)
          word-letters (str/split "STAMP" #"")  ; first letter cannot be S
          expected nil]
      (is (= expected (sut/letters-after-warms gfl word-letters)))))

  (testing "returns nil if a :warm guess letter isn't in the word"
    (let [gf {:guess "SOARE" :feedback "21111"}
          gfl (sut/gf->list gf)
          word-letters (str/split "ABCDE" #"")  ; no S anywhere
          expected nil]
      (is (= expected (sut/letters-after-warms gfl word-letters)))))

  (testing "replaces a matching letter with an underscore"
    (let [gf {:guess "SOARE" :feedback "21111"}
          gfl (sut/gf->list gf)
          word-letters (str/split "XXXXS" #"")
          expected ["X" "X" "X" "X" "_"]]
      (is (= expected (sut/letters-after-warms gfl word-letters)))))

  (testing "replaces the first matching letter with an underscore if more than 1"
    (let [gf {:guess "SOARE" :feedback "21111"}
          gfl (sut/gf->list gf)
          word-letters (str/split "X_XSS" #"")
          expected ["X" "_" "X" "_" "S"]]
      (is (= expected (sut/letters-after-warms gfl word-letters)))))
)  ;; letters-after-warms


(deftest letters-after-colds
  (testing "passes a nil word-letters along as nil"
    (let [gf {:guess "SOARE" :feedback "11111"}
          gfl (sut/gf->list gf)
          word-letters nil
          expected nil]
      (is (= expected (sut/letters-after-colds gfl word-letters)))))

  (testing "returns nil if the word contains a :cold letter"
    (let [gf {:guess "SOARE" :feedback "11111"}  ; no A, B, C, D, or E
          gfl (sut/gf->list gf)
          word "ABCDE"
          word-letters (str/split word #"")
          expected nil]
      (is (= expected (sut/letters-after-colds gfl word-letters)))))

  (testing "passes along word-letters if it doesn't contain any cold letters"
    (let [gf {:guess "SOARE" :feedback "11111"}  ; no A, B, C, D, or E
          gfl (sut/gf->list gf)
          word "_WYXZ"
          word-letters (str/split word #"")
          expected word-letters]
      (is (= expected (sut/letters-after-colds gfl word-letters)))))
)  ;; letters-after-colds


(comment
  (deftest old-tests
    (testing "word-works? correctly removes words that are inconsistent with a gf"
      (let [gf {:guess "SOARE" :feedback "11321"}
            ok-words ["BRAID" "CRAMP" "GRAIL" "TRAIN" "WRATH"]
            bad-words ["BORED" "LISPS" "TUPLE" "APPLE" "HORSE"]
            all-words (set (flatten [ok-words bad-words]))]

        (is (= (count ok-words)
               (count (sut/filter-using-gf gf all-words)))))

      (testing "ASKEW bug from 2022-04-25"
        (let [gfs [{:guess "SOARE" :feedback "21212"},
                   {:guess "DITCH" :feedback "11111"},
                   {:guess "PLANK" :feedback "11212"}]
              remaining-words (sut/filter-using-gfs gfs sut/popular-word-list)
              swims {:guess "SWIMS" :feedback "22111"}
              words-after-swims (sut/filter-using-gf swims remaining-words)]
          (println remaining-words)
      ;; Make sure that ASKEW is consistent with each gf in gfs separately
          (doseq [gf gfs]
            (is (true? (sut/word-works? "ASKEW" gf))))
      ;; Make sure that ASKEW remains after applying all gfs
        ;; (is (true? (contains? remaining-words "ASKEW")))
      ;; ASKEW should still remain after applying SWIMS
        ;; (is (true? (contains? words-after-swims "ASKEW")))
          ))))
  )
