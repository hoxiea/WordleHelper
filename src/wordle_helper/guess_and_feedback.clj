(ns wordle-helper.guess-and-feedback
  "Functionality related to valid guesses and feedback from the user."
  (:require
   [wordle-helper.helpers :as util]
   [clojure.term.colors :as color]
   [clojure.string :as str]))

(declare get-valid-guess-from-user
         get-valid-feedback-from-user)

;; CONSTANTS
(def wordle {:word-length 5
             :num-guesses 6})

;; THE MAIN ATTRACTION
(defn get-user-guess-and-feedback
  "Collect a valid guess + feedback (often called a gf) from the user."
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
  {"1" #(text-color %)
   "2" #(color/on-yellow (text-color %))
   "3" #(color/on-green  (text-color %))})
(defn cold [text] ((get color-fns "1") text))
(defn warm [text] ((get color-fns "2") text))
(defn hot  [text] ((get color-fns "3") text))

;; HELPER FUNCTIONS: FORMATTING GUESS+FEEDBACK
(defn apply-color
  [s feedback-str]
  ((get color-fns feedback-str) s))

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
             (str current (apply-color (util/first-letter g)
                                       (util/first-letter f)))))))

;; HELPER FUNCTIONS: UTILITIES
(defn clean-user-guess
  "Clean a user-entered guess."
  [raw-guess]
  (let [guess-regex #"[a-zA-z]+"]
    (util/extract-join-upper raw-guess guess-regex)))

(defn clean-user-feedback
  "Clean a user-entered guess feedback."
  [raw-feedback]
  (let [feedback-regex #"[123]+"]
  (util/extract-join-upper raw-feedback feedback-regex)))

(defn is-input-valid?
  "Did the user enter a guess OR feedback that's valid after cleaning?"
  [cleaned-input]
  (= (count cleaned-input) (get wordle :word-length)))

(def is-guess-valid? is-input-valid?)
(def is-feedback-valid? is-input-valid?)

;; HELPER FUNCTIONS: INPUT FROM USER
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
                 (get wordle :word-length) "letters!")
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
                 (get wordle :word-length) "numbers 1-3!")
        (get-valid-feedback-from-user guess)))))

(defn gf-confirmed?
  "Check with the player to make sure the g&f they entered is correct.
   Called when the g&f results in 0 words remainings. If they confirm that their
   g&f was entered correctly, then upgrade to the big word list. If there was a
   typo, then just ignore the g&f."
  [gf]
  (println (str "There are 0 words consistent with " (format-gf gf) "!"))
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
