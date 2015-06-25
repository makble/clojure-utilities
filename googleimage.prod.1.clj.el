(def html-prefix "

<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"zh-CN\">	
	
  <head>
    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head><body>
"        
)


(defn get-json [url]
  (let [ d (prototype.Downloader.)
         json-content  (.DownloadPage d url)
       ]
    (println "-start--" (.length json-content) "-end-- url is " url)   
    (clojure.data.json/read-json json-content)
  )
)

(defn query-google-image [query page]
   (let [start (* (- page 1) 4)]
     (get-json (str "http://" proxy-site "/index.php?url=" (query-to-urlparameter (str "https://ajax.googleapis.com/ajax/services/search/images?start=" (str start)  "&v=1.0&q=" (query-to-urlparameter query) )))))
)

(defn image-result-to-html [json]
  (let [results (:results (:responseData json))
        interested (map #(vec [(:originalContextUrl %) (:title %) (:url %) ])  results)
       ]
    (join "</br></br>" (map #(str    "<br/>" (str "<a href=\"" (first %)  "\" ><h2>" (second %) "</h2></a>") "<br/>"  "<img style=\"width:100%\" src=\"" (nth % 2)  "\"/>") interested))
  )
)

(defn pretty-image-result [json]
  (let [results (:results (:responseData json))
        interested (map #(vec [(:originalContextUrl %) (:title %) (:url %)]) results) 
       ]
    (join "\n      ++++++++++++        \n\n" (map #(str "url: " (first %) "\n" "imgurl: " (nth % 2) "\n" "title: " (second %) "\n" ) interested))
  )
)

(defn query-html [query page]
  (let [r1 (future (image-result-to-html (query-google-image query (+ 1 (* 5 (- page 1)) ))))
        r2 (future (image-result-to-html (query-google-image query (+ 2 (* 5 (- page 1)) ))))
        r3 (future (image-result-to-html (query-google-image query (+ 3 (* 5 (- page 1)) ))))
        r4 (future (image-result-to-html (query-google-image query (+ 4 (* 5 (- page 1)) ))))
        r5 (future (image-result-to-html (query-google-image query (+ 5 (* 5 (- page 1)) ))))
       ]

    (spit "c:/tmp/abc.html" (str html-prefix (join "" [
      (deref r1) (deref r2) (deref r3) (deref r4) (deref r5) ] ) "</body>" ))
      )      
  
)

(defn query-html-seq [query page]
  (let [r1 (deref (future (image-result-to-html (query-google-image query (+ 1 (* 5 (- page 1)) )))))
        r2 (deref (future (image-result-to-html (query-google-image query (+ 2 (* 5 (- page 1)) )))))
        r3 (deref (future (image-result-to-html (query-google-image query (+ 3 (* 5 (- page 1)) )))))
        r4 (deref (future (image-result-to-html (query-google-image query (+ 4 (* 5 (- page 1)) )))))
        r5 (deref (future (image-result-to-html (query-google-image query (+ 5 (* 5 (- page 1)) )))))
       ]

    (spit "c:/tmp/abc.html" (str html-prefix (join "" [
       r1  r2  r3  r4 r5 ] ) "</body>" ))
      )      
  
)
