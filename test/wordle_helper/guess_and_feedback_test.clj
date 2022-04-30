(ns wordle-helper.guess-and-feedback-test
  (:require [clojure.test :refer [deftest testing is]]
            [wordle-helper.guess-and-feedback :as gaf]
            [wordle-helper.printer :as wpr]))

(deftest bad-guesses-are-not-invalid
  (testing "Bad raw guesses aren't valid"
      (let [bad-raw-guesses ["" "abc" "too many characters"]
            bad (map gaf/clean-user-guess bad-raw-guesses)]
        (is (every? false? (map gaf/is-guess-valid? bad))) true)))

(deftest good-guesses-are-valid
  (testing "Good raw guesses are valid"
      (let [good-raw-guesses ["soare" "hoxie" "a b c d e"]
            good (map gaf/clean-user-guess good-raw-guesses)]
        (is (every? true? (map gaf/is-guess-valid? good))) true)))

(deftest text-coloring-works
  (testing "Guesses are colored correctly according to feedback received"
      (let [gf {:guess "ABC" :feedback "123"}
            expected (str (wpr/cold "A") (wpr/warm "B") (wpr/hot "C"))]
        (is (= expected (wpr/format-gf gf))))))
