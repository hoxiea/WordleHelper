(ns wordle-helper.config
  "Constants related to the game of Wordle, and settings for Wordle Helper.")

;; Constants related to Wordle
(def params {:word-length 5
             :num-guesses 6})

;; These options will be set:
;; - here and via user input, for the CLI version
;; - in-game, for the browser extension version
(def game-options (atom {:hard-mode? false
                         :high-contrast-mode? true
                         :dark-mode? true}))

(defn toggle-high-contrast-mode!
  "Invert the current boolean value of (:high-contrast-mode? @game-options)."
  []
  (swap! game-options update-in [:high-contrast-mode?] not))

;; These options are:
;; - set permanently, for the CLI version
;; - changeable via the extension settings, for the browser extension
(def display-options {:num-top-letters 10
                      :num-best-words 10})
