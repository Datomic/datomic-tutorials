(ns todo-db
  (:require
   [datomic.api :as d]))

(def schema
  [{:db/ident       :list/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "List name"}
   {:db/ident       :list/items
    :db/valueType   :db.type/ref;; reference
    :db/cardinality :db.cardinality/many
    :db/doc         "List items reference"}
   {:db/ident       :item/status
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc         "Item Status"}
   {:db/ident       :item/text
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Item text"}])

(def db-uri "datomic:dev://localhost:4334/todo")

;; INFO: Creates the database, if it does not exist returns false
(d/create-database db-uri) ;; it requires a running transactor

;; INFO: to delete a database use `d/delete-database`
(comment (d/delete-database db-uri))

;; Connect to the database
(def conn (d/connect db-uri))

(defn transact-schema
  "receives a `schema` and transacts it"
  [schema]
  @(d/transact conn schema))

(defn new-list
  "receives a `list-name` and returns a new List datom."
  [list-name]
  {:list/name list-name})

(defn new-item
  "recives a `db` a `list-name` and the `item-text` and returns a map form of datoms to add items to a list."
  [db list-name item-text]
  {:db/id (d/entid db [:list/name list-name])
   :list/items [{:db/id (d/tempid :db.part/user)
                 :item/text item-text
                 :item/status :item.status/todo}]})

;; Get lists and their names
(d/q '[:find ?list-name
       :in $
       :where [?list :list/name ?list-name]]
  (d/db conn))

;; Get lists + items
(d/q '[:find ?list-name ?items
       :in $
       :where [?list :list/name ?list-name]
              [?list :list/items ?items]]
  (d/db conn))

;; Get lists + items different version
(d/q '[:find ?list-name (vec ?items)
       :in $
       :where [?list :list/name ?list-name]
              [?list :list/items ?items]]
     (d/db conn))

;; Get lists + items using pull
(d/q '[:find (pull ?list [:list/name {:list/items [:item/text]}])
       :in $
       :where [?list :list/name ?list-name]]
     (d/db conn))