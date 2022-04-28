(ns wordle-helper.helpers-test
  (:require [wordle-helper.helpers :as sut]
            [clojure.test :as t]))

(t/deftest nth-letter-test
  (t/testing "nth-letter works as expected"
    (t/is (= (sut/nth-letter "ABCDE" 0) "A"))
    (t/is (= (sut/nth-letter "ABCDE" 1) "B"))
    (t/is (= (sut/nth-letter "ABCDE" 2) "C"))
    (t/is (= (sut/nth-letter "ABCDE" 3) "D"))
    (t/is (= (sut/nth-letter "ABCDE" 4) "E"))))
