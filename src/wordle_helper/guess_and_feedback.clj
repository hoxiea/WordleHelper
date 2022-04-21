(ns wordle-helper.guess-and-feedback
  "Functionality related to getting Wordle valid guesses and feedback from the user."
  (:require
   [clojure.string :as str]
   [clojure.term.colors :as color]))

(declare get-valid-guess-from-user get-valid-feedback-from-user)

;; CONSTANTS
(def wordle 
  {:word-length 5
   :num-guesses 6})
(def guess-regex #"[a-zA-z]+")
(def feedback-regex #"[123]+")

;; THE MAIN ATTRACTION
(defn get-user-guess-and-feedback
  "Collect a valid guess and its feedback from the user."
  []
  (let [valid-guess (get-valid-guess-from-user)
        valid-feedback (get-valid-feedback-from-user valid-guess)]
    {:guess valid-guess, :feedback valid-feedback}))

;; FEEDBACK INFORMATION
;; "1" - letter doesn't appear in word
;; "2" - letter appears in word, but not in guessed position
;; "3" - letter appears in word, in guessed position
(def text-color color/white)
(def color-fns
  {"1" #(color/on-red    (text-color %))
   "2" #(color/on-yellow (text-color %))
   "3" #(color/on-green  (text-color %))})
(defn cold [text] ((get color-fns "1") text))
(defn warm [text] ((get color-fns "2") text))
(defn hot  [text] ((get color-fns "3") text))

;; HELPER FUNCTIONS: FORMATTING GUESS+FEEDBACK
(defn apply-color 
  [g f]
  ((get color-fns f) g))

(defn format-gf 
  "Format a guess (using feedback) for printing to terminal."
  [{:keys [guess feedback]}]
  (loop [g guess 
         f feedback 
         current ""]
    (if (= g "")
      current
      (recur (subs g 1) 
             (subs f 1) 
             (str current (apply-color (subs g 0 1) (subs f 0 1)))))))


;; HELPER FUNCTIONS: UTILITIES
(defn extract-join-upper
  "Extract all substrings matching `re`, join, and convert to uppercase."
  [s re]
  (-> (re-seq re s)
      str/join
      str/upper-case))

(defn clean-user-guess
  "Clean a user-entered guess."
  [raw-guess]
  (extract-join-upper raw-guess guess-regex))

(defn clean-user-feedback
  "Clean a user-entered guess feedback."
  [raw-feedback]
  (extract-join-upper raw-feedback feedback-regex))

(defn is-input-valid?
  "Did the user enter a guess OR feedback that's valid after cleaning?"
  [cleaned-input]
  (= (count cleaned-input) (get wordle :word-length)))

(def is-guess-valid? is-input-valid?)
(def is-feedback-valid? is-input-valid?)

(defn pad-with-space
  "Add a space to the end of a string, if it doesn't already have one."
  [s]
  (str s (if (str/ends-with? s " ") "" " ")))

;; HELPER FUNCTIONS: INPUT FROM USER
(defn get-raw-input
  "Get a line of raw user input from *in*."
  [prompt]
  (print (pad-with-space prompt))
  (flush)
  (read-line))

(defn get-valid-guess-from-user
  "Get a valid guess from the user."
  []
  (let [guess-prompt "What did you guess?"
        raw-guess (get-raw-input guess-prompt)
        clean-guess (clean-user-guess raw-guess)]
    (if (is-guess-valid? clean-guess)
      clean-guess
      (do
        (println "Make sure your guess contains exactly" (get wordle :word-length) "letters!")
        (get-valid-guess-from-user)))
  ))

(defn get-valid-feedback-from-user
  "Get valid guess feedback from the user."
  [guess]
  (let [prompt (str "And what did Wordle tell you about your guess, " guess "?")
        raw-feedback (get-raw-input prompt)
        clean-feedback (clean-user-feedback raw-feedback)]
    (if (is-feedback-valid? clean-feedback)
      clean-feedback
      (do
        (println "Make sure your feedback contains exactly" (get wordle :word-length) "numbers 1-3!")
        (get-valid-feedback-from-user guess)))
  ))

;; Use guess feedback to eliminate possible words
(defn word-works?
  "Is `word` consistent with the guess and feedback?"
  [word {:keys [guess feedback]}]
    (loop [idx 0]
      (if (= idx (count word))
        true
        (let [word-letter (subs word idx (inc idx))
              guess-letter (subs guess idx (inc idx))
              feedback-digit (subs feedback idx (inc idx))]
          (cond
            (and (= feedback-digit "3") (not (= word-letter guess-letter))) false
            (and (= feedback-digit "1") (str/includes? word guess-letter)) false
            (and (= feedback-digit "2") (or (= word-letter guess-letter)
                                            (not (str/includes? word guess-letter)))) false
            :else (recur (inc idx)))
          ))))
