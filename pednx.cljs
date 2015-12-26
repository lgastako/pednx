#!/usr/bin/env planck
(ns pednx.cli
  (:import [goog.string StringBuffer])
  (:require [cljs.core :refer [*out*]]
            [cljs.reader :refer [read-string]]
            [planck.core :refer [*command-line-args* *in*
                                 IClosable IReader
                                 Reader Writer
                                 slurp spit]]
            [planck.io :as io]))

(def serialize   (comp #(str % "\n") pr-str))
(def deserialize read-string)

(defn p
  "Print `args` to the screen as a vector."
  [& args]
  (println (mapv pr-str args)))

(defn not-implemented
  "Raise a not implemented exception with the provided `args`."
  [& args]
  (throw (ex-info (str "not-implemented: " args) {:args args})))

(defn cmd-name
  "Return the program name from the command line."
  [args]
  ;; (aget args 0)
  args)

(defn fn<-cmd [cmd]
  (case cmd
    "assoc" assoc
    "assoc-in" assoc-in
    "count" count
    "dissoc" dissoc
    "update" update
    "update-in" update-in))

(defrecord CloseIgnorer [in]
  IReader
  (-read [_]
    (-read in))
  IClosable
  (-close [_]))

(defn <-stdin []
  (p :<-stdin)
  (with-redefs [planck.core/*reader-fn* (comp identity first)]
    (slurp (CloseIgnorer. *in*)))
  #_
  (let [r  *in*;;(Reader. js/PLANCK_RAW_READ_STDIN nil)
        sb (StringBuffer.)]
    (p :<-stdin {:r r})
    (loop [s (-read r)]
      (if (nil? s)
        (.toString sb)
        (do
          (.append sb s)
          (recur (-read r)))))))

(defn read-input [fn]
  (p :read-input {:fn fn})
  (let [bytes (if (= fn "-")
                (<-stdin)
                (slurp fn))]
    (deserialize bytes)))


(defn ->stdout [s]
  (p :->stdout {:s s})
  (let [w (Writer. js/PLANCK_RAW_WRITE_STDOUT
                   js/PLANCK_RAW_FLUSH_STDOUT
                   nil)]
    (-write w s)))

(defn write-output [fn data]
  (p ::write-output {:fn fn :data data})
  (let [serialized (serialize data)]
    (if (= fn "-")
      (->stdout serialized)
      (spit fn serialized))))

(defn fn-name [f]
  ;; :redacted
  ;; f
  (.-name f)
  )

(defn invoke [f fn args]
  (p :invoke {:f (fn-name f) :fn fn :args args})
  (let [data (read-input fn)
        data' (apply f data args)]
    (p :invoke {:data data})
    (p :invoke {:data' data'})
    (write-output fn data')))

(defn deserialize-args [args]
  (map read-string args))

(defn parse-args [args]
  ;; Ideally we'd be able to look at argv[0] and get the command name, but haven't
  ;; figured out how to do that with planck yet, so, for now we'll require the
  ;; first arg to be the command name
  {:cmd (first args)
   :error nil
   :fn (second args)
   :args (deserialize-args (rest (rest args)))})

(defn boom [& args]
  (throw (ex-info "boom" {:boom-args args})))

(def symbol->fn-map
  {'inc inc
   'dec dec
   'first first
   'second second
   'rest rest
   'last last
   'butlast butlast
   'count count})

(defn sym->f [sym]
  (p :sym->f {:sym sym})
  (if-let [f (symbol->fn-map sym)]
    f
    boom))

(defn customize-update-args [args]
  (update (vec args) 1 sym->f))

(defn get-arg-customizer [cmd]
  (case cmd
    "update" customize-update-args
    identity))

(defn customize-args [cmd args]
  (p :customize-args {:cmd cmd :args args})
  (if-let [arg-customizer (get-arg-customizer cmd)]
    (do
      (p :customize-args :customizing...)
      (arg-customizer args))
    args))

(defn -main [& args]
  (p :-main {:args args})
  (let [{:keys [cmd args fn error]} (parse-args args)]
    (p {:cmd cmd :args args :error error})
    (if error
      (p :error error)
      (if-let [f (fn<-cmd cmd)]
        (let [args (customize-args cmd args)]
          (invoke f fn args))
        (p :error :unknown-command {:cmd cmd})))))

(apply -main *command-line-args*)
