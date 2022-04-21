(ns wordle-helper.guess-and-feedback-test
  (:require [clojure.test :refer :all]
            [wordle-helper.guess-and-feedback :refer :all]))

(deftest bad-guesses-are-not-invalid
  (testing "Bad raw guesses aren't valid"
      (let [bad-raw-guesses ["" "abc" "too many characters"]
            bad (map clean-user-guess bad-raw-guesses)]
        (is (every? false? (map is-guess-valid? bad))) true)))

(deftest good-guesses-are-valid
  (testing "Good raw guesses are valid"
      (let [good-raw-guesses ["soare" "hoxie" "a b c d e"]
            good (map clean-user-guess good-raw-guesses)]
        (is (every? true? (map is-guess-valid? good))) true)))

(deftest text-coloring-works
  (testing "Guesses are colored correctly according to feedback received"
      (let [gf {:guess "ABC" :feedback "123"}
            expected (str (cold "A") (warm "B") (hot "C"))]
        (is (= expected (format-gf gf))))))

(deftest word-filtering-works
  (testing "word-works? correctly removes words that are inconsistent with a gf"
      (let [gf {:guess "SOARE" :feedback "11321"}
            ok-words ["BRAID" "CRAMP" "GRAIL" "TRAIN" "WRATH"]
            bad-words ["BORED" "LISPS" "TUPLE" "APPLE" "HORSE"]
            all-words (set (flatten [ok-words bad-words]))]
            
        (is (= (count ok-words) (count (filter #(word-works? % gf) all-words)))))))