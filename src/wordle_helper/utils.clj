(ns wordle-helper.utils
  (:require [clojure.string :as str]))


(defn extract-join-upper
  "Extract all substrings of s matching `re`, join, and convert to uppercase."
  [s re]
  (-> (re-seq re s)
      str/join
      str/upper-case))

(defn pad-with-space
  "Add a space to the end of a string, if it doesn't already have one."
  [s]
  (str s (if (str/ends-with? s " ") "" " ")))

(defn get-raw-input
  "Prompt the user for a line of input, then read user response."
  [prompt]
  (print (pad-with-space prompt))
  (flush)
  (read-line))

(defn nth-letter
  "Get the 0-indexed nth letter of a String as a String, not a character."
  [s n]
  {:pre [(< n (count s))]}
  (subs s n (inc n)))

(defn first-letter
  "Get the first letter of a String as a String, not a character."
  [s]
  (nth-letter s 0))

(defn vector-contains?
  "Does vector `vec` contain an element equal to `x`?"
  [vec x]
  (not= -1 (.indexOf vec x)))

(defn print-sorted
  "Print the provided strings in sorted order, one per line, with a newline at
   the end."
  [strings]
  (doseq [s (sort strings)]
    (println s))
  (println))
