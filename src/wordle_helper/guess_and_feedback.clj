(ns wordle-helper.guess-and-feedback
  "Functionality related to valid guesses and feedback from the CLI."
  (:require
   [clojure.string :as str]
   [wordle-helper.utils :as util]
   [wordle-helper.config :refer [params]]))

;; When you enter a guess into Wordle, you receive feedback about each letter of
;; your guess. This feedback is incredibly helpful in reducing the space of
;; possible words down to the correct answer.
;;
;; In order to help you solve the Wordle, the Wordle Helper needs to collect a
;; valid word and some valid feedback from the user.

;; A guess can be entered via a seq of five lowercase or uppercase letters:
(def guess-regex (re-pattern (str "[a-zA-Z]{" (:word-length params) "}")))

;; Guess feeedback is entered via a sequence of five numbers, with meaning:
(def feedback->temp {"1" :cold
                     "2" :warm
                     "3" :hot})
(def feedback-regex (re-pattern (str "[" (str/join (keys feedback->temp)) "]"
                                     "{" (:word-length params)            "}")))

(defn process-raw-input
  "Extract the first match of `input` against `re` and convert to uppercase,
  or nil if no matches."
  [re input]
  (let [match (re-find re input)]
    (when match
      (str/upper-case match))))

(def process-guess (partial process-raw-input guess-regex))
(def process-feedback (partial process-raw-input feedback-regex))

(comment
  (process-guess "abcdefg") ;; => "ABCDE"
  (process-guess "only the first long word");; => "FIRST"
  (process-guess "no long word here");; => nil
  (process-feedback " 11223 ") ;; => "11223"
  (process-feedback " 112423 ") ;; => nil: the 4 ruins it
  )

(defn to-gf
  "Convert separate guess and feedback Strings into a gf."
  [guess feedback]
  (let [letters (str/split guess #"")
        temps (map feedback->temp (str/split feedback #""))]
    (util/zip letters temps)))

(comment
  (to-gf "SOARE" "12123")
;; => (["S" :cold] ["O" :warm] ["A" :cold] ["R" :warm] ["E" :hot])
  )

;;;; IMPURE I/O FUNCTIONS
;; Put it all together to get valid guess and valid feedback...
(defn get-valid-guess-from-user
  "Get a valid guess from the user."
  ([prompt]
   (let [raw-guess (util/get-raw-input prompt)
         clean-guess (process-guess raw-guess)]
     (if clean-guess
       clean-guess
       (do
         (println "Make sure your guess contains exactly"
                  (:word-length params) "letters!")
         (get-valid-guess-from-user)))))
  ([] (get-valid-guess-from-user "What did you guess?")))

(defn get-valid-feedback-from-user
  "Get valid guess feedback from the user: a sequence of five {1,2,3}s."
  [guess]
  (let [prompt (str "And what did Wordle tell you about " guess "?")
        raw-feedback (util/get-raw-input prompt)
        clean-feedback (process-feedback raw-feedback)]
    (if clean-feedback
      clean-feedback
      (do
        (println "Make sure your feedback contains exactly"
                 (get params :word-length) "numbers 1-3!")
        (get-valid-feedback-from-user guess)))))

(defn get-user-guess-and-feedback
  "Collect a valid guess + feedback (i.e. a 'gf') from the user."
  []
  (let [valid-guess (get-valid-guess-from-user)
        valid-feedback (get-valid-feedback-from-user valid-guess)]
    (to-gf valid-guess valid-feedback)))
