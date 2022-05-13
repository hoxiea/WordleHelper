(ns wordle-helper.utils
  (:require [clojure.string :as str]))


;;;; STRINGS
(defn pad-with-space
  "Add a space to the end of a string if it doesn't already have one."
  [s]
  (str s (if (str/ends-with? s " ") "" " ")))

(defn nth-letter
  "Get the 0-indexed nth letter of a String as a String, not a character."
  [s n]
  {:pre [(< n (count s))]}
  (subs s n (inc n)))

(defn first-letter
  "Get the first letter of a String as a String, not a character."
  [s]
  (nth-letter s 0))

;;;; VECTORS
(defn vector-contains?
  "Does vector `vec` contain an element equal to `x`?"
  [vec x]
  (not= -1 (.indexOf vec x)))

(defn zip
  "Pair up corresponding elements from seq1 and seq2."
  [seq1 seq2]
  (map vector seq1 seq2))

;;;; MAPS
(defn n-largest-vals
  "Get the `n` largest key-value pairs from map `m`."
  [m n]
  (->> m (sort-by val >) (take n)))

;;;; INPUT
(defn get-raw-input
  "Prompt the user for a line of input, then read user response."
  [prompt]
  (print (pad-with-space prompt))
  (flush)
  (read-line))

(defn get-user-choice
  "Get a valid choice from choices: symbol -> String via *in*."
  [choices]
  (let [valid-options (set (map name (keys choices)))]
    (loop []
      (println "What would you like to do?")
      (doseq [[choice descr] choices]
        (println (str (name choice) ": " descr)))

      (let [in (-> (read-line) str/trim str/lower-case)]
        (cond
          (= in "")
          (do (println "Please select an option!\n")
              (recur))

          (contains? valid-options (first-letter in))
          (keyword (first-letter in))

          :else
          (do (println "\nInvalid option - please try again!\n")
              (recur)))))))

(comment
  (def choices {:s "Do a thing"
                :t "Do another thing"})
  (get-user-choice choices)  ;; type "s"
;; => :s
  (get-user-choice choices)  ;; inputs other than "s" and "t" are not accepted
;; => :t
  )

;;;; OUTPUT
(defn print-sorted
  "Print the provided strings in sorted order, one per line,
  with a newline at the end."
  [strings]
  (doseq [s (sort strings)]
    (println s))
  (println))
