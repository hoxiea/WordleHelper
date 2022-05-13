(ns wordle-helper.guess-and-feedback-test
  (:require [clojure.test :refer [deftest testing is]]
            [wordle-helper.guess-and-feedback :as sut]))

(deftest process-guess-test
  (testing "five capital letters are recognized"
    (is (= (sut/process-guess "SOARE") "SOARE")))
  (testing "lowercase letters are fine too"
    (is (= (sut/process-guess "soare") "SOARE")))
  (testing "the first consecutive 5 letters are taken"
    (is (= (sut/process-guess "my fav opener is soare") "OPENE")))
  (testing "if no 5+ letter words are given, return nil"
    (is (= (sut/process-guess "only tiny wrds here") nil))))

(deftest process-feedback-test
  (testing "five numbers between 1 and 3 are recognized"
    (is (= (sut/process-feedback "11223") "11223")))
  (testing "grab the first five if more than five numbers in {1, 2, 3} given"
    (is (= (sut/process-feedback "11223311") "11223")))
  (testing "nil if <5 numbers in {1, 2, 3} given"
    (is (= (sut/process-feedback "123") nil)))
  (testing "nil if 5 *consecutive* numbers in {1, 2, 3} aren't given"
    (is (= (sut/process-feedback "123 soare 321") nil))
    (is (= (sut/process-feedback "1234321") nil))))

(deftest to-gf-test
  (testing "guess & feedback get zipped, with feedback changed to temperatures"
    (let [exp '(["S" :cold] ["O" :warm] ["A" :hot] ["R" :warm] ["E" :cold])]
      (is (= (sut/to-gf "SOARE" "12321") exp)))))
