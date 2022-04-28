(ns wordle-helper.wordlist-test
  (:require [clojure.test :refer [deftest testing is]]
            [wordle-helper.wordlist :as sut]))

(deftest word-filtering-works
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
        )))
  )
