;;
;; for Mac
;; 980401/af converted to DM2
;; 18/10/2000/af added delay box, synths, ballon help


(in-package :dm)


(defvar *music-changed* nil)
(defvar *geneva9* '("Geneva" 9 :srccopy :plain))

(defclass edit-music-window (window)())

;toplevel called by the music loader
;(defun make-or-update-edit-music-window ()
; (setq *music-changed* nil)
;  (if (not (windows :class 'edit-music-window))
;    (make-edit-music-window)
;    (update-edit-music-window) ))

(defun make-or-update-edit-music-window ()
 (setq *music-changed* nil)
  (if (windows :class 'edit-music-window)
    (dolist (win (windows :class 'edit-music-window))
      (window-close win) ))
  (make-edit-music-window) )

;; ------------------------
;;   VIEW-UPDATE-CONTENTS
;; ------------------------
;;
(defmethod view-update-contents ((self edit-music-window))
  (set-window-title self (nickname *active-score*))
  (do-subviews (subview self)
    (when (numberp (view-nick-name subview))
      (dialog-item-update 
       subview 
       (nth (1- (view-nick-name subview)) (track-list *active-score*))
      ))))

;; ----------------------------
;;   UPDATE-EDIT-MUSIC-WINDOW
;; ----------------------------
;;
(defun update-edit-music-window ()
  (dolist (window (windows :class 'edit-music-window))
    (view-update-contents window) ))


;; --------------------------
;;   MAKE-EDIT-MUSIC-WINDOW
;; --------------------------
;;

(defun make-edit-music-window ()
  (with-cursor *watch-cursor*
    (let ((ypos 15)
          (my-edit-music-window
           (make-instance 'edit-music-window
             :view-size #@(367 106)
             :window-title (or (nickname *active-score*) "untitled")
             :help-spec "track window"
             :window-show nil
             :view-subviews 
             (list
              (make-dialog-item 'static-text-dialog-item                            ;voice number
                                #@(0 0) #@(40 16) "Track" nil :view-font *geneva9* 
                                :help-spec "Track number" )
              (make-dialog-item 'static-text-dialog-item                            ;active
                                #@(30 0) #@(35 16) "Active" nil :view-font *geneva9*
                                :help-spec "Playing and rule application only for Active tracks" )
              (make-dialog-item 'static-text-dialog-item                            ;name
                                #@(67 0) #@(40 16) "Name" nil :view-font *geneva9*
                                :help-spec "Track name" )
              (make-dialog-item 'static-text-dialog-item                            ;synth
                                #@(160 0) #@(40 16) "Synth" nil :view-font *geneva9*
                                :help-spec "The selected synth. object defines the conversion of perf. variables to midi" )
              (make-dialog-item 'static-text-dialog-item                            ;channel
                                #@(272 0) #@(40 16) "Ch" nil :view-font *geneva9*
                                :help-spec "Initial midi channel. Range: 1 to 16")
              (make-dialog-item 'static-text-dialog-item                            ;program
                                #@(295 0) #@(40 16) "Pr" nil :view-font *geneva9*
                                :help-spec "Initial midi program number. Range: 1 to 128" )
              (make-dialog-item 'static-text-dialog-item                            ;volume
                                #@(320 0) #@(40 16) "Vol" nil :view-font *geneva9*
                                :help-spec "Initial track volume. Range: 0 to -64 dB" )
              (make-dialog-item 'static-text-dialog-item                            ;delay
                                #@(343 0) #@(40 16) "Del" nil :view-font *geneva9*
                                :help-spec "Initial track delay (ms)" )
              ))))
      (let ((itrack 1))
        (dolist (track (track-list *active-score*))
              (add-subviews 
               my-edit-music-window
               (make-instance 'edit-music-dialog-item
                 :view-position (make-point 0 ypos)
                 :track track
                 :voice-nr (prin1-to-string  itrack)
                 :view-font *geneva9*
                 :view-nick-name itrack) )
              (incf ypos 18)
              (incf itrack)
              ))
      (view-update-contents my-edit-music-window)
      (set-window-zoom-size my-edit-music-window 317 305)
      (set-back-color my-edit-music-window (make-color (* 257 152) (* 257 201) (* 257 224))) ;*light-blue-color*
      (window-show my-edit-music-window)
      )))

;dialog with 7 subviews: voice number, enable, name, channel, synt, pr, vol
(defclass edit-music-dialog-item (view)())
   

;; -----------------------
;;   INITIALIZE-INSTANCE
;; -----------------------
;;

(defmethod initialize-instance ((item edit-music-dialog-item) &rest initargs
                                &key track (voice-nr "  1")
                                     (name "")(channel "")(pr "")(vol "")(delay "") )
  (declare (ignore initargs))
  (call-next-method)
  (set-view-size item #@(370 18))
 ;(print track)
  (add-subviews item
         (make-dialog-item 'static-text-dialog-item             ;voice number
                           #@(0 0)
                           #@(20 16)
                           voice-nr
                           nil
                           :view-nick-name 'voice
                           :help-spec "Track number"
                            )
         (make-dialog-item 'check-box-dialog-item               ;active
                           #@(37 0)
                           #@(16 16)
                           ""
                           #'(lambda (item) item
                              (cond (track
                                     ;(setq *music-changed* t)
                                     (if (check-box-checked-p item)
                                       (setf (active-p track) t)
                                       (setf (active-p track) nil))
                                     )
                                    (t (check-box-uncheck item)) ))
                           :view-nick-name 'active
                           :help-spec "Playing and rule application only for Active tracks"
                           :check-box-checked-p (if track (active-p track) nil) )
         (make-dialog-item 'editable-text-dialog-item           ;name
                           #@(67 3)
                           #@(77 12)
                           name
                           #'(lambda (item) 
                              (cond (track
                                     (setq *music-changed* t)
                                     (if (string= "" (dialog-item-text item))
                                       (setf (trackname track) "")
                                       (setf (trackname track) (dialog-item-text item)) ))
                                    (t (set-dialog-item-text item "")) ))
                           :view-font *geneva9*
                           :help-spec "Track name"
                           :allow-returns nil
                           :view-nick-name 'name )
         (make-instance 'pop-up-menu                            ;synt
           :view-position #@(147 0)
           :view-size #@(120 19)
           :view-font *geneva9*
           :help-spec "The selected synth. object defines the conversion of perf. variables to midi"
           :view-nick-name 'synt
           :menu-items (make-synth-menu-item-list track) )
         (make-dialog-item 'editable-text-dialog-item           ;channel
                           #@(268 3)
                           #@(20 12)
                           channel
                           #'(lambda (item)
                              (cond 
                               (track
                                (setq *music-changed* t)
                                (if (string= "" (dialog-item-text item))
                                  (setf (midi-channel track) nil)
                                  (if (channel-within-limits? (read-from-string (dialog-item-text item)))
                                    (setf (midi-channel track) (read-from-string (dialog-item-text item)))
                                    (progn (set-dialog-item-text item "1")
                                           (setf (midi-channel track) (read-from-string (dialog-item-text item)))
                                           ))))
                                    (t (set-dialog-item-text item "")) ))
                           :view-font *geneva9*
                           :help-spec "Initial midi channel. Range: 1 to 16"
                           :allow-returns nil
                           :view-nick-name 'channel
                           :justification :right)


         (make-dialog-item 'editable-text-dialog-item           ;MIDI program
                           #@(292 3)
                           #@(20 12)
                           pr
                           #'(lambda (item)
                               (cond 
                                (track
                                 (setq *music-changed* t)
                                 (if (string= "" (dialog-item-text item))
                                   (setf (midi-initial-program track) nil)
                                   (if (program-within-limits? (read-from-string (dialog-item-text item)))
                                     (setf (midi-initial-program track) (read-from-string (dialog-item-text item)))
                                     (progn (set-dialog-item-text item "1")
                                            (setf (midi-initial-program track) (read-from-string (dialog-item-text item)))
                                            ))))
                                (t (set-dialog-item-text item "")) ))
                           :view-font *geneva9*
                           :justification :right
                           :help-spec "Initial midi program number. Range: 1 to 128"
                           :allow-returns nil
                           :view-nick-name 'program
                           )
         (make-dialog-item 'editable-text-dialog-item           ; volume
                           #@(317 3)
                           #@(20 12)
                           vol
                           #'(lambda (item)
                               (cond 
                                (track
                                 (setq *music-changed* t)
                                 (if (string= "" (dialog-item-text item))
                                   (setf (midi-initial-volume track) nil)
                                   (if (not (string= "-" (dialog-item-text item)))
                                     (if (volume-within-limits? (read-from-string (dialog-item-text item)))
                                       (setf (midi-initial-volume track) (read-from-string (dialog-item-text item)))
                                       (progn (set-dialog-item-text item "0")
                                              (setf (midi-initial-volume track) (read-from-string (dialog-item-text item)))
                                              )))))
                                (t (set-dialog-item-text item "")) ))
                           :view-font *geneva9*
                           :justification :right
                           :help-spec "Initial track volume. Range: 0 to -64 dB"
                           :allow-returns nil
                           :view-nick-name 'volume)
         (make-dialog-item 'editable-text-dialog-item           ;initial delay
                           #@(342 3)
                           #@(20 12)
                           delay
                           #'(lambda (item)
                               (cond 
                                (track
                                 (setq *music-changed* t)
                                 (if (string= "" (dialog-item-text item))
                                   (setf (track-delay track) 0)
                                   (if (delay-within-limits? (read-from-string (dialog-item-text item)))
                                     (setf (track-delay track) (read-from-string (dialog-item-text item)))
                                     (progn (set-dialog-item-text item "0")
                                            (setf (track-delay track) (read-from-string (dialog-item-text item)))
                                            ))))
                                (t (set-dialog-item-text item "")) ))
                           :view-font *geneva9*
                           :justification :right
                           :help-spec "Initial track delay (ms)"
                           :allow-returns nil
                           :view-nick-name 'delay)

            ))

;returns a list of menu-items corresonding to all synths in the list *defined-synths*
(defun make-synth-menu-item-list (track)
  (let ((menu-list '()))
    (dolist (synth-pair *defined-synths*)
      (newr menu-list (make-synth-menu-item track (car synth-pair))) )
    menu-list ))

(defun make-synth-menu-item (track synth-name)
  (make-instance 'menu-item :menu-item-title synth-name
              :menu-item-action
              #'(lambda () (setq *music-changed* t)
                 (setf (synth track) (make-synth synth-name)) )))




;; ----------------------
;;   DIALOG-ITEM-UPDATE
;; ----------------------
;;
;; update all data from the track object



(defmethod dialog-item-update ((item edit-music-dialog-item) track)
  (cond (track
         (if (active-p track)
             (check-box-check (view-named 'active item))
             (check-box-uncheck (view-named 'active item)) )
         (if (trackname track)
           (set-dialog-item-text (view-named 'name item) (trackname track))
           (set-dialog-item-text (view-named 'name item) "") )
         (if (midi-channel track)
           (set-dialog-item-text (view-named 'channel item) (prin1-to-string (midi-channel track)))
           (set-dialog-item-text (view-named 'channel item) "") )
         (if (midi-initial-program track)
           (set-dialog-item-text (view-named 'program item) (prin1-to-string (midi-initial-program track)))
           (set-dialog-item-text (view-named 'program item) "") )
         (if (midi-initial-volume track)
           (set-dialog-item-text (view-named 'volume item) (prin1-to-string (midi-initial-volume track)))
           (set-dialog-item-text (view-named 'volume item) "") )
         (if (track-delay track)
           (set-dialog-item-text (view-named 'delay item) (prin1-to-string (track-delay track)))
           (set-dialog-item-text (view-named 'delay item) "") )
         (if (synth track)
             (setf (ccl::pop-up-menu-default-item (view-named 'synt item))
                   (synth-symbol-to-index (type-of (synth track))) )
             (setf (ccl::pop-up-menu-default-item (view-named 'synt item)) 1) )
         ;(with-font-focused-view (view-container (view-named 'synt item)) (view-draw-contents (view-named 'synt item)))
         )
         ))

(defun channel-within-limits? (nr)
 (if (and (integerp nr)
          (< nr 17)
          (> nr 0) )
   t
   nil))
(defun program-within-limits? (nr)
 (if (and (integerp nr)
          (< nr 129)
          (> nr 0) )
   t
   nil))
(defun volume-within-limits? (nr)
 (if (and (integerp nr)
          (<= nr 0)
          (>= nr -99) )
   t
   nil))
(defun delay-within-limits? (nr)
 (if (and (integerp nr)
          (< nr 1000)
          (> nr 0) )
   t
   nil))
