(ns wordle-helper.config
  "Constants related to the game of Wordle, and settings for Wordle Helper."
  (:require
    [clojure.term.colors :as color]))

(def options {:hard-mode? false})

(def colors {:text-color color/white
             :cold-bg color/on-grey
             :warm-bg color/on-yellow
             :hot-bg color/on-green})

(defn color-text [text] ((:text-color colors) text))

(defn cold [text] ((:cold-bg colors) (color-text text)))
(defn warm [text] ((:warm-bg colors) (color-text text)))
(defn hot [text] ((:hot-bg colors) (color-text text)))

(def params {:word-length 5
             :num-guesses 6})
