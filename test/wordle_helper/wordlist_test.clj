(ns wordle-helper.wordlist-test
  (:require [clojure.test :refer [deftest testing is]]
            [wordle-helper.wordlist :refer [word-works?]]))

(deftest word-filtering-works
  (testing "word-works? correctly removes words that are inconsistent with a gf"
      (let [gf {:guess "SOARE" :feedback "11321"}
            ok-words ["BRAID" "CRAMP" "GRAIL" "TRAIN" "WRATH"]
            bad-words ["BORED" "LISPS" "TUPLE" "APPLE" "HORSE"]
            all-words (set (flatten [ok-words bad-words]))]
            
        (is (= (count ok-words) (count (filter #(word-works? % gf) all-words)))))))