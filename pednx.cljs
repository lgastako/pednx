#!/usr/bin/env planck
(ns pednx.cli
  (:import  [goog.string    StringBuffer])
  (:require [cljs.core      :refer [*out*]]
            [cljs.reader    :refer [read-string]]
            [clojure.string :as str]
            [planck.core    :refer [*command-line-args* *in*
                                    IClosable IReader
                                    Reader Writer
                                    -read
                                    slurp spit]]
            [planck.io      :as io]))

(defn boom [& args]
  (throw (ex-info "boom" {:boom-args args})))

(def serialize   prn-str)      ;; porn star
(def deserialize read-string)

(defn p
  "Print `args` to the screen as a vector."
  [& args]
  (println (mapv pr-str args)))

(defn not-implemented
  "Raise a not implemented exception with the provided `args`."
  [& args]
  (throw (ex-info (str "not-implemented: " args) {:args args})))


(defn pred [x]
  ;; TODO: How do we include these fns with question marks
  ;; e.g. "nil?"
  ;;      nilq
  ;;      nilp
  ;;      nil_q
  ;;      nil_pred
  (str/replace x #"\\?" "p"))

;; what about the backwards arg'd ones? can we flip them?
;; "drop"      drop
;; "partition" partition
;; "take"      take
;; bunch more, see https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/core.cljs

(defn fn<-cmd [cmd]
  ;; (p :fn<-cmd {:cmd cmd})
  (if-let [fn (case cmd
                ;; TODO: automate from whitelist
                "="           =
                "+"           +
                "-"           -
                "/"           clojure.core//
                "*"           *
                ">"           >
                "<"           <
                "<="          <=
                ">="          >=
                "aget"        aget
                "alength"     alength
                "apply"       apply
                "aset"        aset
                "assoc"       assoc
                "assoc-in"    assoc-in
                "boolean"     boolean
                "butlast"     butlast
                "concat"      concat
                "conj"        conj
                "count"       count
                "distinct"    distinct
                "dec"         dec
                "disj"        disj
                "dissoc"      dissoc
                "empty"       empty
                "ffirst"      ffirst
                "find"        find
                "first"       first
                "filter"      filter
                "filterv"     filterv
                "flatten"     flatten
                "fnext"       fnext
                "fnil"        fnil
                "frequencies" frequencies
                "get"         get
                "inc"         inc
                "identity"    identity
                "last"        last
                "list"        list
                "keep"        keep
                "key"         key
                "keys"        keys
                "keyword"     keyword
                "map"         map
                "mapv"        mapv
                "mapcat"      mapcat
                "max"         max
                "min"         min
                "name"        name
                "nfirst"      nfirst
                "next"        next
                "nnext"       nnext
                "not"         not
                "nth"         nth
                "nthnext"     nthnext
                "nthrest"     nthrest
                "peek"        peek
                "pop"         pop
                "pr-str"      pr-str
                "prn-str"     prn-str
                "quot"        quot
                "reduce"      reduce
                "rem"         rem
                "remove"      remove
                "rest"        rest
                "reverse"     reverse
                "second"      second
                "select-keys" select-keys
                "set"         set
                "shuffle"     shuffle
                "some"        some
                "sort"        sort
                "sorted-map"  sorted-map
                "str"         str
                "subs"        subs
                "subvec"      subvec
                "symbol"      symbol
                "update"      update
                "update-in"   update-in
                "val"         val
                "vals"        vals
                "vec"         vec
                "vector"      vector
                ;; predicate fns in the form of `x?` become commands in the form of `xp`
                "associativep" associative?
                "charp"        char?
                "collp"        coll?
                "countedp"     counted?
                "containsp"    contains?
                "distincp"     distinct
                "emptyp"       empty?
                "evenp"         even?
                "everyp"       every?
                "falsep"       false?
                "fnp"          fn?
                "indexedp"     indexed?
                "integep"      integer?
                "iterablep"    iterable?
                "keywordp"     keyword?
                "listp"        list?
                "mapp"         map?
                "negp"         neg?
                "nilp"         nil?
                "not-anyp"     not-any?
                "not-everyp"   not-every?
                "numberp"      number?
                "oddp"         odd?
                "objectp"      object?
                "posp"         pos?
                "setp"         set?
                "seqp"         seq?
                "seqablep"     seqable?
                "sequentialp"  sequential?
                "somep"        some?
                "sortedp"      sorted?
                "stringp"      string?
                "symbolp"      symbol?
                "truep"        true?
                "undefinedp"   undefined?
                "vectorp"      vector?
                "zerop"        zero?
                nil)]
    fn
    ;; TODO: real usage
    (throw (ex-info (str "usage: fixme " cmd) {:cmd cmd}))))

(defrecord CloseIgnorer [in]
  IReader
  (-read [_]
    (-read in))
  IClosable
  (-close [_]))

(defn <-stdin []
  ;; (p :<-stdin)
  (let [r  *in*
        sb (StringBuffer.)]
    (loop [s (-read r)]
      (if (nil? s)
        (.toString sb)
        (do
          (.append sb s)
          (recur (-read r)))))))

(defn read-input [fn]
  ;; (p :read-input {:fn fn})
  (try
    (let [bytes (if (= fn "-")
                  (<-stdin)
                  (slurp fn))]
      ;; (p :read-input {:bytes bytes})
      (deserialize bytes))
    (catch :default ex
      (p :read-input {:ex ex})
      nil)))


(defn ->stdout [s]
  ;; (p :->stdout {:s s})
  (let [w (Writer. js/PLANCK_RAW_WRITE_STDOUT
                   js/PLANCK_RAW_FLUSH_STDOUT
                   nil)]
    (-write w s)))

(defn create-if-not-exists! [fn]
  ;; (p :create-if-not-exists! {:fn fn})
  ;;  (not-implemented "create-if-not-exists!")
  ;; seems to be working without this.  if we don't run into any more trouble
  ;; soon then rip this out.
  )

(defn spit+ [fn data]
  ;; (p :spit+ {:fn fn :data data})
  (create-if-not-exists! fn)
  (spit fn data))

(defn write-output [fn data]
  ;; (p ::write-output {:fn fn :data data})
  (let [serialized (serialize data)]
    (if (= fn "-")
      (->stdout serialized)
      (spit+ fn serialized))))

(defn fn-name [f]
  ;; :redacted
  ;; f
  (.-name f)
  )

(defn invoke [f fn args]
  ;; (p :invoke {:f (fn-name f) :fn fn :args args})
  (let [data (read-input fn)
        data' (apply f data args)]
    ;; (p :invoke {:data data})
    ;; (p :invoke {:data' data'})
    (write-output fn data')))

(defn deserialize-args [args]
  (map read-string args))

(defn parse-args [args]
  ;; Ideally we'd be able to look at argv[0] and get the command name, but haven't
  ;; figured out how to do that with planck yet, so, for now we'll require the
  ;; first arg to be the command name
  ;; (p :parse-args {:args args})
  {:cmd (first args)
   :error nil
   :fn (second args)
   :args (deserialize-args (rest (rest args)))})

;; These are the functions that can be passed to update
(def symbol->fn-map
  ;; How to automate without access to resolve/ns-resolve?
  ;; All the predicates need to be in here too.
  {'butlast butlast
   'count   count
   'dec     dec
   'first   first
   'inc     inc
   'last    last
   'rest    rest
   'second  second})

(defn sym->f [sym]
  ;; (p :sym->f {:sym sym})
  (if-let [f (symbol->fn-map sym)]
    f
    boom))

(defn get-arg-customizer [cmd]
  (letfn [(nth-sym->f [n]
            (fn [args]
              (update (vec args) n sym->f)))]
    (case cmd
      "apply"     (nth-sym->f 1)
      "filter"    (nth-sym->f 1)
      "filterv"   (nth-sym->f 1)
      "keep"      (nth-sym->f 1)
      "map"       (nth-sym->f 1)
      "mapcat"    (nth-sym->f 1)
      "reduce"    (nth-sym->f 1)
      "remove"    (nth-sym->f 1)
      "update"    (nth-sym->f 1)
      "update-in" (nth-sym->f 1)
      identity)))

(defn customize-args [cmd args]
  ;; (p :customize-args {:cmd cmd :args args})
  (if-let [arg-customizer (get-arg-customizer cmd)]
    (do
      ;; (p :customize-args :customizing...)
      (arg-customizer args))
    args))

(defn -main [& args]
  ;; (p :-main {:args args})
  (let [{:keys [cmd args fn error] :as parsed} (parse-args args)]
    ;; (p :parsed parsed)
    ;; (p {:cmd cmd :args args :error error})
    (if error
      (p :error error)
      (if-let [f (fn<-cmd cmd)]
        (let [args (customize-args cmd args)]
          (invoke f fn args))
        (p :error :unknown-command {:cmd cmd})))))

(apply -main *command-line-args*)
