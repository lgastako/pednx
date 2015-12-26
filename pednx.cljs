(ns pednx.cli
  (:require [cljs.reader :refer [read-string]]
            [planck.core :refer [*command-line-args* slurp]]))

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
    "dissoc" dissoc))

(defn read-input [fn]
  (p :read-input {:fn fn})
  (try
    (when-let [bytes (slurp fn)]
      (p :read-input {:bytes bytes})
      (read-string bytes))
    (catch :default ex
      nil)))

(defn write-output [fn data])

(defn invoke [f fn args]
  (p :invoke {:f :redacted :fn fn :args args})
  (let [data (read-input fn)
        data' (apply f data args)]
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

(defn -main [& args]
  (p :-main {:args args})
  (let [{:keys [cmd args fn error]} (parse-args args)]
    (p {:cmd cmd :args args :error error})
    (if error
      (p :error error)
      (if-let [f (fn<-cmd cmd)]
        (invoke f fn args)
        (p :error :unknown-command {:cmd cmd})))))

(apply -main *command-line-args*)
