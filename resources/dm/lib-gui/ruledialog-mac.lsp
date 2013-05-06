;;;-*-Mode: LISP; Package: DM -*-;;;; *****************************************;;   Rule dialog and rule setups: MAC part;; *****************************************;;;; 9203 AndersF;; 9711 Vittorio;;;; this file has a "twin", a version for Win95. The common functions;; moved to the file ruledialog-common (that will be maybe fused with another):;;   open-rule-set;;   default-open-rule-set;;   rule-call-list-to-string;;   string-to-rule-call-list(in-package :dm)(defclass apply-rules-window (window)  ((pathname :initarg :pathname))  );;opens the rule setup window with the rules as defined in;;the slots all-rules and sync-rule-list(defun edit-rule-set (&key pathname)  (with-cursor *watch-cursor*  (let ((my-window          (make-instance 'apply-rules-window                        :view-size #@(615 400)                        :view-position '(:top 40)                        :window-title (if pathname (file-namestring pathname) "Rule palette")                        :window-show nil                        :color-p t                        :pathname pathname))        (xcolumn1 2)(ypos 4)(xcolumn2 95)(yincr 17)(ymax))    (set-back-color my-window (make-color (* 257 146) (* 257 201) (* 257 183)))    ;(set-back-color my-window 15000804)    (let ((ypos ypos))      (dolist (rulepair (get-dm-var 'all-rules))        (cond          ((numberp (cadr rulepair)) ;rule with quantity          (add-subviews           my-window           (make-instance 'rule-quant-dialog-item                          :view-position (make-point xcolumn2 ypos)                          :setting (cadr rulepair)                          :dialog-item-text (string-capitalize (rule-call-list-to-string rulepair))                          :view-nick-name 'rule-quant)))         (t          (add-subviews            ;rule without quantity           my-window           (make-instance 'rule-check-box-dialog-item                          :view-position (make-point (+ xcolumn2 107) ypos)                          :view-font '("Geneva" 9 :srccopy :plain)                          :dialog-item-text (string-capitalize (symbol-name (car rulepair)))                           :check-box-checked-p (cadr rulepair)                          :view-nick-name 'rule-dig))))        (incf ypos yincr))      (setq ymax ypos))    (do-subviews (item my-window 'rule-quant-dialog-item)  ;fix- update the scroll bar again      (set-rule-setting item (rule-setting item)))    (add-subviews       my-window       (make-instance 'button-dialog-item                      :view-position (make-point xcolumn1 2)                      :view-size #@(90 20)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "Apply"                      :view-nick-name 'button                      :dialog-item-action                       #'(lambda (item) (apply-rules (view-container item)))))    (add-subviews       my-window       (make-instance 'button-dialog-item                      :view-position (make-point xcolumn1 24)                      :view-size #@(90 20)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "Init & Apply"                      :view-nick-name 'button                      :dialog-item-action                       #'(lambda (item)                           ;(if (get-filename)                           ;  (load-music-fpath (get-filename) :redraw nil)                           ;  (error (format nil "no filename, music not saved to file")))                          (reset-music)                           (apply-rules (view-container item)))))    (add-subviews       my-window       (make-instance 'button-dialog-item                      :view-position (make-point xcolumn1 46)                      :view-size #@(90 20)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "Init, Apply & Play"                      :view-nick-name 'button                      :dialog-item-action                       #'(lambda (item)                           ;(if (get-filename)                           ;  (load-music-fpath (get-filename) :redraw nil)                           ;  (error (format nil "no filename, music not saved to file")))                          (reset-music)                           (apply-rules (view-container item))                           (eval-enqueue '(playlist)))))    (add-subviews       my-window       (make-instance 'button-dialog-item                      :view-position (make-point xcolumn1 68)                      :view-size #@(90 20)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "Init & Play"                      :view-nick-name 'button                      :dialog-item-action                       #'(lambda (item) item                           ;(if (get-filename)                           ;  (load-music-fpath (get-filename) :redraw nil)                           ;  (error (format nil "no filename, music not saved to file")))                          (reset-music)                           (eval-enqueue '(playlist)))))    (add-subviews       my-window       (make-instance 'button-dialog-item                      :view-position (make-point xcolumn1 90)                      :view-size #@(90 20)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "Save as..."                      :view-nick-name 'button                      :dialog-item-action                       #'(lambda (item)                           (save-rule-set-as (view-container item)))                       ))    (add-subviews       my-window       (make-instance 'button-dialog-item                      :view-position (make-point xcolumn1 112)                      :view-size #@(50 20)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "Scale :"                      :view-nick-name 'button                      :dialog-item-action                       #'(lambda (item)                           (scale-all-rules (view-container item)))                       ))    (add-subviews       my-window       (make-instance 'editable-text-dialog-item                      :view-position (make-point (+ 55 xcolumn1) 116)                      :view-size #@(32 12)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text "1.5"                      :view-nick-name 'scale-factor-setting                       ))    (setq ypos 140)    (dolist (rulepair (get-dm-var 'sync-rule-list))      (add-subviews       my-window       (make-instance 'radio-button-dialog-item                      :view-position (make-point xcolumn1 ypos)                      :view-font '("Geneva" 9 :srccopy :plain)                      :dialog-item-text (string-capitalize (symbol-name (car rulepair)))                       :radio-button-pushed-p (cadr rulepair)                      :view-nick-name 'rule-sync))      (incf ypos yincr))    (set-view-size my-window (point-h (view-size my-window)) (+ 2 (max ymax ypos)))    (do-subviews (subview my-window)      (if       (eq (view-nick-name subview) 'button)       (set-part-color subview :body 16766656) ))    ;(set-back-color my-window 15000804)    (window-show my-window))))          ;extracts the rule list from the window;in the format for the 'apply-rule-list functions(defmethod rule-list ((item apply-rules-window))  (let ((l))    (do-subviews (subview item)      (cond       ((and (eq (view-nick-name subview) 'rule-quant)             (not (zerop (rule-setting subview)))             (not (equal "" (dialog-item-text subview))))        (newr l (string-to-rule-call-list                  (dialog-item-text subview)                 (rule-setting subview) )))       ((and (eq (view-nick-name subview) 'rule-dig)             (check-box-checked-p subview)             (not (equal "" (dialog-item-text subview))))        (newr l (list (read-from-string (dialog-item-text subview)))))       ))    l))(defmethod apply-rules ((item apply-rules-window))  (with-cursor *watch-cursor*    (do-subviews (subview item)      (when (and (eq (view-nick-name subview) 'rule-sync)                 (radio-button-pushed-p subview))        (rule-apply-list-sync         (rule-list item)         (read-from-string (dialog-item-text subview)))        ))    (redraw-display-windows)));extracts the rule list from the window;in the load and save rule setups format(defmethod rule-list-all ((item apply-rules-window))  (let ((l))    (do-subviews (subview item)      (cond       ((eq (view-nick-name subview) 'rule-quant)        (newr l (list (read-from-string (dialog-item-text subview))                      (rule-setting subview) )))       ((eq (view-nick-name subview) 'rule-dig)        (newr l (list (read-from-string (dialog-item-text subview))                      (check-box-checked-p subview))))       ))    l))(defmethod rule-list-all ((item apply-rules-window))  (let ((l))    (do-subviews (subview item)      (cond       ((and (eq (view-nick-name subview) 'rule-quant)             (not (equal "" (dialog-item-text subview))) )        (newr l (string-to-rule-call-list                  (dialog-item-text subview)                 (rule-setting subview) )))       ((and (eq (view-nick-name subview) 'rule-dig)             (not (equal "" (dialog-item-text subview))) )        (newr l (list (read-from-string (dialog-item-text subview))                      (check-box-checked-p subview))))       ))    l));; save a rule set in a file(defmethod save-rule-set-as ((item apply-rules-window))  (let ((fpath          ;(choose-new-file-dialog         ; :directory (merge-pathnames  ".pal" (or (slot-value item 'pathname) "temp")))         (choose-new-file-dialog          :directory (slot-value item 'pathname))         ))    ;(print fpath)    (if fpath       (progn        (with-open-file (ofile fpath :direction :output                                :if-does-not-exist :create                               :if-exists :supersede)          (let ((*package* (find-package "DM")))            (with-cursor *watch-cursor*              (princ "(in-package \"DM\")" ofile)(terpri ofile)              (princ "(set-dm-var 'all-rules '(" ofile)(terpri ofile)              (dolist (rule (rule-list-all item))                (prin1 rule ofile)(terpri ofile) )              (princ "))" ofile)(terpri ofile)                                (princ "(set-dm-var 'sync-rule-list '(" ofile)              (do-subviews (subview item)                (when (eq (view-nick-name subview) 'rule-sync)                  (princ "(" ofile)                  (princ (dialog-item-text subview) ofile)                  (princ " " ofile)                  (princ (radio-button-pushed-p subview) ofile)                  (princ ")" ofile) ))              (princ "))" ofile)(terpri ofile)              )))        (set-window-title item (file-namestring fpath))        (setf (slot-value item 'pathname) fpath)         ))));; scale all rules with scrollbar with the amount given;; in the scale-factor-setting dialog item;; something wrong with the updating of the scrollbars ;; graphically.(defmethod scale-all-rules ((item apply-rules-window))  (let ((scale-factor         (read-from-string          (dialog-item-text           (view-named 'scale-factor-setting item)))))    (do-subviews (subview item)      (if (eq (view-nick-name subview) 'rule-quant)        (set-rule-setting subview (* scale-factor (rule-setting subview))) ))));;------------- rule dialog item definition ---------------------------------------;;-- rule with scroll-bar and number input ------------------------;dialog with 3 subviews: scroll-bar, editable number and editable rule-name(defclass rule-quant-dialog-item (view)())   (defmethod initialize-instance ((item rule-quant-dialog-item) &rest initargs                                &key (setting 1.0) (dialog-item-text "") )  (declare (ignore initargs))  (call-next-method)  (set-view-size item #@(510 18))  (add-subviews item     (make-dialog-item 'editable-text-dialog-item                       #@(3 3)                       #@(25 12)                       "0.0"                       #'(lambda (item)                           (if (and (string/= (dialog-item-text item) "")                                    (numberp (read-from-string (dialog-item-text item))))                             (progn                               (let ((setting (read-from-string (dialog-item-text item)))                                     (scroll (find-named-sibling item 'scroll-bar)) )                                 (set-scroll-bar-setting scroll (round (* setting 10)))                                 ;(set-rule-colour scroll setting)                                 ))))                       :draw-outline t                       :view-font '("Geneva" 9 :srccopy :plain)                       :allow-returns nil                       :view-nick-name 'number)     (make-dialog-item 'scroll-bar-dialog-item                       #@(30 8)                       #@(100 10)                       "scroll"                       #'(lambda (item &aux (setting (format nil "~a"                                                             (/ (scroll-bar-setting item)                                                                10.0))))                           ;(print "scroll")                           ;(set-rule-colour item (scroll-bar-setting item))                           (set-dialog-item-text                            (find-named-sibling item 'number)                            setting)                           (window-update-event-handler (view-window item)))                       :direction :horizontal                       ;:width 10                       :view-nick-name 'scroll-bar                       :min -50                       :max 50                       :setting 0                       :track-thumb-p t)     (make-dialog-item 'editable-text-dialog-item                       #@(132 3)                       #@(368 12)                       dialog-item-text                       nil                       :draw-outline t                       :view-font '("Geneva" 9 :srccopy :plain)                       :allow-returns nil                       :view-nick-name 'text)     )  (set-rule-setting item setting)  )(defmethod rule-setting ((item rule-quant-dialog-item))  (read-from-string   (dialog-item-text (view-named 'number item))))(defmethod set-rule-setting ((item rule-quant-dialog-item) setting)  (let ((scroll (view-named 'scroll-bar item)))   (set-dialog-item-text (view-named 'number item)                         (format nil "~a" setting))   (set-scroll-bar-setting scroll (round (* setting 10)))   ;(set-rule-color scroll setting)   ))(defmethod dialog-item-text ((item rule-quant-dialog-item))  (dialog-item-text (view-named 'text item)))(defmethod set-dialog-item-text ((item rule-quant-dialog-item) text)  (set-dialog-item-text (view-named 'text item) text));funkar ej p� quadran ???(defmethod set-rule-color ((item scroll-bar-dialog-item) setting)  (print-ll "set-rule-color: " item "   " setting)   (cond ((zerop setting)          (set-part-color item :thumb *white-color*)          ;(set-part-color item :frame *black-color*)          ;(set-part-color item :body *tan-color*)          )         ((plusp setting)          (set-part-color item :thumb *red-color*)          ;(set-part-color item :frame *red-color*)          ;(set-part-color item :body *red-color*)          )         ((minusp setting)          (set-part-color item :thumb *blue-color*)          ;(set-part-color item :frame *blue-color*)          ;(set-part-color item :body *blue-color*)          ) ));;-- rule with scroll-bar, checkbox for sync-rule and number input ------------------------#| not used;dialog with 4 subviews: scroll-bar, checkbox for sync, editable number and editable rule-name(defclass rule-quant-sync-dialog-item (view)())   (defmethod initialize-instance ((item rule-quant-sync-dialog-item) &rest initargs                                &key (setting 1.0) (sync-check-box-checked-p t)                                (dialog-item-text "") )  (declare (ignore initargs))  (call-next-method)  (set-view-size item #@(335 12))  (add-subviews item     (make-dialog-item 'check-box-dialog-item                       #@(0 0)                       #@(35 12)                       ""                       nil                       :check-box-checked-p sync-check-box-checked-p                       :view-nick-name 'sync-check-box)     (make-dialog-item 'editable-text-dialog-item                       #@(35 0)                       #@(25 10)                       "0.0"                       #'(lambda (item)                           (if (and (string/= (dialog-item-text item) "")                                    (numberp (read-from-string (dialog-item-text item))))                             (progn                               (let ((setting (read-from-string (dialog-item-text item)))                                     (scroll (find-named-sibling item 'scroll-bar)) )                                 (set-scroll-bar-setting scroll (round (* setting 10)))                                 (set-rule-colour scroll setting)                                 ))))                       :draw-outline nil                       :view-font '("Geneva" 9 :srccopy :plain)                       :allow-returns nil                       :view-nick-name 'number)     (make-dialog-item 'scroll-bar-dialog-item                       #@(65 0)                       #@(100 10)                       "scroll"                       #'(lambda (item &aux (setting (format nil "~a"                                                             (/ (scroll-bar-setting item)                                                                10.0))))                           (set-rule-colour item (scroll-bar-setting item))                           (set-dialog-item-text                            (find-named-sibling item 'number)                            setting)                           (window-update-event-handler (view-window item)))                       :direction :horizontal                       :width 10                       :view-nick-name 'scroll-bar                       :min -50                       :max 50                       :setting 0)     (make-dialog-item 'editable-text-dialog-item                       #@(170 0)                       #@(165 12)                       dialog-item-text                       nil                       :draw-outline nil                       :view-font '("Geneva" 9 :srccopy :plain)                       :allow-returns nil                       :view-nick-name 'text)     )  (set-rule-setting item setting)  )(defmethod rule-setting ((item rule-quant-sync-dialog-item))  (read-from-string   (dialog-item-text (view-named 'number item))))(defmethod set-rule-setting ((item rule-quant-sync-dialog-item) setting)  (let ((scroll (view-named 'scroll-bar item)))   (set-dialog-item-text (view-named 'number item)                         (format nil "~a" setting))   (set-scroll-bar-setting scroll (round (* setting 10)))   (set-rule-colour scroll setting)))(defmethod dialog-item-text ((item rule-quant-sync-dialog-item))  (dialog-item-text (view-named 'text item)))(defmethod set-dialog-item-text ((item rule-quant-sync-dialog-item) text)  (set-dialog-item-text (view-named 'text item) text))(defmethod sync-check-box-checked-p ((item rule-quant-sync-dialog-item))  (check-box-checked-p (view-named 'sync-check-box item)))(defmethod sync-check-box-check ((item rule-quant-sync-dialog-item))  (check-box-check (view-named 'sync-check-box item)))(defmethod sync-check-box-uncheck ((item rule-quant-sync-dialog-item))  (check-box-uncheck (view-named 'sync-check-box item)))(defmethod set-rule-colour ((item scroll-bar-dialog-item) setting)   (cond ((zerop setting)          (set-part-color item :thumb *white-color*))         ((plusp setting)          (set-part-color item :thumb *red-color*))         ((minusp setting)          (set-part-color item :thumb *blue-color*)) ))|#;;-- rule with check-box ------------------------(defclass rule-check-box-dialog-item (view)())   (defmethod initialize-instance ((item rule-check-box-dialog-item) &rest initargs                                &key (check-box-checked-p nil) (dialog-item-text "") )  (declare (ignore initargs))  (call-next-method)  (set-view-size item #@(310 18))  (add-subviews item     (make-dialog-item 'check-box-dialog-item                       #@(-2 6)                       #@(14 12)                       ""                       nil                       :check-box-checked-p check-box-checked-p                       :view-nick-name 'check-box)     (make-dialog-item 'editable-text-dialog-item                       #@(25 3)                       #@(275 12)                       dialog-item-text                       nil                       :draw-outline t                       :view-font '("Geneva" 9 :srccopy :plain)                       :allow-returns nil                       :view-nick-name 'text)     ))(defmethod check-box-checked-p ((item rule-check-box-dialog-item))  (check-box-checked-p (view-named 'check-box item)))(defmethod check-box-check ((item rule-check-box-dialog-item))  (check-box-check (view-named 'check-box item)))(defmethod check-box-uncheck ((item rule-check-box-dialog-item))  (check-box-uncheck (view-named 'check-box item)))(defmethod dialog-item-text ((item rule-check-box-dialog-item))  (dialog-item-text (view-named 'text item)))(defmethod set-dialog-item-text ((item rule-check-box-dialog-item) text)  (set-dialog-item-text (view-named 'text item) text));;-- rule with check-box and sync-check-box ------------------------(defclass rule-check-box-sync-dialog-item (view)())   (defmethod initialize-instance ((item rule-check-box-sync-dialog-item) &rest initargs                                &key (check-box-checked-p nil) (sync-check-box-checked-p t)                                (dialog-item-text "") )  (declare (ignore initargs))  (call-next-method)  (set-view-size item #@(300 12))  (add-subviews item     (make-dialog-item 'check-box-dialog-item                       #@(0 0)                       #@(35 12)                       ""                       nil                       :check-box-checked-p sync-check-box-checked-p                       :view-nick-name 'sync-check-box)     (make-dialog-item 'check-box-dialog-item                       #@(35 0)                       #@(25 12)                       ""                       nil                       :check-box-checked-p check-box-checked-p                       :view-nick-name 'check-box)     (make-dialog-item 'editable-text-dialog-item                       #@(60 0)                       #@(275 12)                       dialog-item-text                       nil                       :draw-outline nil                       :view-font '("Geneva" 9 :srccopy :plain)                       :allow-returns nil                       :view-nick-name 'text)     ))(defmethod check-box-checked-p ((item rule-check-box-sync-dialog-item))  (check-box-checked-p (view-named 'check-box item)))(defmethod check-box-check ((item rule-check-box-sync-dialog-item))  (check-box-check (view-named 'check-box item)))(defmethod check-box-uncheck ((item rule-check-box-sync-dialog-item))  (check-box-uncheck (view-named 'check-box item)))(defmethod sync-check-box-checked-p ((item rule-check-box-sync-dialog-item))  (check-box-checked-p (view-named 'sync-check-box item)))(defmethod sync-check-box-check ((item rule-check-box-sync-dialog-item))  (check-box-check (view-named 'sync-check-box item)))(defmethod sync-check-box-uncheck ((item rule-check-box-sync-dialog-item))  (check-box-uncheck (view-named 'sync-check-box item)))(defmethod dialog-item-text ((item rule-check-box-sync-dialog-item))  (dialog-item-text (view-named 'text item)))(defmethod set-dialog-item-text ((item rule-check-box-sync-dialog-item) text)  (set-dialog-item-text (view-named 'text item) text))