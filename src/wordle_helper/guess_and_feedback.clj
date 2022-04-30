(ns wordle-helper.guess-and-feedback
  "Functionality related to valid guesses and feedback from the user."
  (:require
   [wordle-helper.utils :as util]
   [wordle-helper.printer :as wpr]
   [wordle-helper.config :refer [params]]
   [clojure.string :as str]))

;; When you enter a guess into Wordle, you receive feedback about each letter of
;; your guess. This feedback is incredibly helpful in reducing the space of
;; possible words down to the correct answer.
;;
;; In order to help you solve the Wordle, the Wordle Helper needs to collect a
;; valid word and some valid feedback from the user.

;; Users might choose to enter their guess lowercase, uppercase, with spaces,
;; etc. We can clean up their input by extracting all letters and converting
;; them to uppercase.
(defn clean-user-guess
  "Clean a user-entered guess."
  [raw-guess]
  (let [guess-regex #"[a-zA-z]+"]
    (util/extract-join-upper raw-guess guess-regex)))

;; Users enter their feedback as a length-5 sequence from {1, 2, 3}, where:
;; 1: cold (miss)
;; 2: warm (partial hit)
;; 3: hot (correct)
;; Again, we'll extract the numbers from their input to get the feedback.
(defn clean-user-feedback
  "Clean a user-entered guess feedback."
  [raw-feedback]
  (let [feedback-regex #"[123]+"]
  (util/extract-join-upper raw-feedback feedback-regex)))

(defn is-input-valid?
  "Did the user enter a guess OR feedback that's valid after cleaning?"
  [cleaned-input]
  (= (count cleaned-input) (get params :word-length)))

(def is-guess-valid? is-input-valid?)
(def is-feedback-valid? is-input-valid?)

;; Put it all together to get valid guess and valid feedback...
(defn get-valid-guess-from-user
  "Get a valid guess from the user."
  []
  (let [guess-prompt "What did you guess?"
        raw-guess (util/get-raw-input guess-prompt)
        clean-guess (clean-user-guess raw-guess)]
    (if (is-guess-valid? clean-guess)
      clean-guess
      (do
        (println "Make sure your guess contains exactly"
                 (get params :word-length) "letters!")
        (get-valid-guess-from-user)))))

(defn get-valid-feedback-from-user
  "Get valid guess feedback from the user."
  [guess]
  (let [prompt (str "And what did Wordle tell you about your guess, " guess "?")
        raw-feedback (util/get-raw-input prompt)
        clean-feedback (clean-user-feedback raw-feedback)]
    (if (is-feedback-valid? clean-feedback)
      clean-feedback
      (do
        (println "Make sure your feedback contains exactly"
                 (get params :word-length) "numbers 1-3!")
        (get-valid-feedback-from-user guess)))))

(defn get-user-guess-and-feedback
  "Collect a valid guess + feedback (often called a gf) from the user."
  []
  (let [valid-guess (get-valid-guess-from-user)
        valid-feedback (get-valid-feedback-from-user valid-guess)]
    {:guess valid-guess, :feedback valid-feedback}))


;; TODO: finish this implementation
(defn gf-confirmed?
  "Check with the player to make sure the g&f they entered is correct.
   Called when the g&f results in 0 words remainings. If they confirm that their
   g&f was entered correctly, then upgrade to the big word list. If there was a
   typo, then just ignore the g&f."
  [gf]
  (println (str "There are 0 words consistent with " (wpr/format-gf gf) "!"))
  (let [prompt "Are you sure you entered your guess & feedback correctly? (Y/n)"
        raw-input (util/get-raw-input prompt)]
    (cond
      (or (empty? raw-input)
          (= "y" (str/lower-case (util/first-letter raw-input))))
      true

      (= "n" (str/lower-case (util/first-letter raw-input)))
      false

      :else
      (do (println "Input not recognized.")
          (gf-confirmed? gf)))))
