(ns clojure-recipes.core
  (:use [clojure.string])
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as xzip]))

;; (defn foo
;;   "I don't do a whole lot."
;;   [x]
;;   (println x "Hello, World!"))

(def root (zip/xml-zip (xml/parse "../recipes.xml")))

;;; Titles returned by this can have a lot of extra leading and trailing
;;; whitespace. Remove it with (map trim (get-titles book)).
;; (defn get-titles
;;   "Returns all recipes titles in cookbook book."
;;   [book]
;;   (map zip/node
;;          (map zip/down
;;               (xzip/xml-> book :recipe :title))))
;;; This improved method shows that we really don't need a function
;;; for this, as we might as well just use xzip/xml-> directly
(defn get-titles
  "Returns all recipe titles in cookbook book"
  [book]
  (xzip/xml-> root :recipe :title xzip/text))

;; (defn get-value [book & tags]
;;   (apply xzip/xml1-> book (conj (vec tags) xzip/text)))

;;; This trick finds and returns the node(s?) that match the 
;;; predicate :author == "Tara Cyphers"
;;; (xzip/xml-> root :recipe :recipeinfo :author "Tara Cyphers")

;;; This method shows how to target a specific recipe based on its
;;; title, and extract its ingredient quantity, unit, and food
;;; items. With all three calls combined, you can grab an entire
;;; recipe's ingredients list to generate a shopping list or whatever

;;; Quantity of each ingredient
(xzip/xml-> root :recipe [:title " Asian Cucumber Salad "] :ingredientlist :ingredient :quantity xzip/text)
;;; Unit type of each ingredient measurement
(xzip/xml-> root :recipe [:title " Asian Cucumber Salad "] :ingredientlist :ingredient :unit xzip/text)
;;; Finally, the food item for each ingredient
(xzip/xml-> root :recipe [:title " Asian Cucumber Salad "] :ingredientlist :ingredient :fooditem xzip/text)

;;; When matching with strings as in the above three examples, the use
;;; of strings really just causes the predicate generator
;;; xzip/text= to compare the text of the node with the
;;; string. This is a great shortcut, but we need to be able to do
;;; substring matches so that the user can search by keyword rather
;;; than by literal strings. The following function returns a regexp
;;; predicate for substring matching:
(defn re-text=
  "Returns a query predicate that matches a node when its
textual content contains the substring s."
  [s] (fn [loc] (re-find (re-pattern s) (xzip/text loc))))

;;; Now, we can grab recipes that contain a given substring (this is
;;; particularly useful for matching category tags, as recipes
;;; typically have several tags per category node. To grab all side
;;; dishes:

(xzip/xml-> root :recipe [:recipeinfo :category (re-text= "Side")] :title xzip/text)

;;; We can use logical OR in our regexps as well to search for recipes
;;; matching multiple criteria. Here we match any recipes that are
;;; classified as entrees or Asian:

(xzip/xml-> root
               :recipe
               [:recipeinfo :category (re-text= "Entree|Asian")]
               :title xzip/text)

;;; The vector notation allows us to make even more complex searches
;;; by stacking up sub-queries. Here we search for vegan recipes that
;;; contain mint:

(xzip/xml-> root
               :recipe
               [:recipeinfo :category (re-text= "Vegan")]
               [:ingredientlist :ingredient :fooditem (re-text= "mint")]
               :title xzip/text)
