FasdUAS 1.101.10   ??   ??    k             l        	  x     ?? 
 ??   
 1      ??
?? 
ascr  ?? ??
?? 
minv  m         ?    2 . 4??       Yosemite (10.10) or later    	 ?   4   Y o s e m i t e   ( 1 0 . 1 0 )   o r   l a t e r      x    ?? ????    2  	 ??
?? 
osax??        l     ????????  ??  ??        i        I      ?? ???? 0 write_to_file        o      ???? 0 target_file     ??  o      ???? 0 data_to_write  ??  ??    k            r     	   !   I    ?? " #
?? .rdwropenshor       file " o     ???? 0 target_file   # ?? $??
?? 
perm $ m    ??
?? boovtrue??   ! o      ???? 0 myfile myFile   % & % I  
 ?? ' (
?? .rdwrwritnull???     **** ' o   
 ???? 0 data_to_write   ( ?? )??
?? 
refn ) o    ???? 0 myfile myFile??   &  *?? * I   ?? +??
?? .rdwrclosnull???     **** + o    ???? 0 myfile myFile??  ??     , - , l     ????????  ??  ??   -  . / . l     ????????  ??  ??   /  0 1 0 i     2 3 2 I      ?? 4???? 20 addleadingzerostonumber addLeadingZerosToNumber 4  5 6 5 o      ???? 0 	thenumber 	theNumber 6  7?? 7 o      ???? 00 themaxleadingzerocount theMaxLeadingZeroCount??  ??   3 k     n 8 8  9 : 9 l     ?? ; <??   ; * $ Determine if the number is negative    < ? = = H   D e t e r m i n e   i f   t h e   n u m b e r   i s   n e g a t i v e :  > ? > r      @ A @ A     B C B o     ???? 0 	thenumber 	theNumber C m    ????   A o      ???? 0 
isnegative 
isNegative ?  D E D l   ????????  ??  ??   E  F G F l   ?? H I??   H B < Determine when the maximum number of digits will be reached    I ? J J x   D e t e r m i n e   w h e n   t h e   m a x i m u m   n u m b e r   o f   d i g i t s   w i l l   b e   r e a c h e d G  K L K r     M N M c     O P O l   	 Q???? Q a    	 R S R m    ???? 
 S o    ???? 00 themaxleadingzerocount theMaxLeadingZeroCount??  ??   P m   	 
??
?? 
long N o      ???? 0 thethreshold theThreshold L  T U T l   ????????  ??  ??   U  V W V l   ?? X Y??   X A ; If the number is shorter than the maximum number of digits    Y ? Z Z v   I f   t h e   n u m b e r   i s   s h o r t e r   t h a n   t h e   m a x i m u m   n u m b e r   o f   d i g i t s W  [?? [ Z    n \ ]?? ^ \ A    _ ` _ o    ???? 0 	thenumber 	theNumber ` o    ???? 0 thethreshold theThreshold ] k    g a a  b c b l   ?? d e??   d 8 2 If the number is negative, convert it to positive    e ? f f d   I f   t h e   n u m b e r   i s   n e g a t i v e ,   c o n v e r t   i t   t o   p o s i t i v e c  g h g Z   " i j???? i =     k l k o    ???? 0 
isnegative 
isNegative l m    ??
?? boovtrue j r     m n m d     o o o    ???? 0 	thenumber 	theNumber n o      ???? 0 	thenumber 	theNumber??  ??   h  p q p l  # #????????  ??  ??   q  r s r l  # #?? t u??   t "  Add the zeros to the number    u ? v v 8   A d d   t h e   z e r o s   t o   t h e   n u m b e r s  w x w r   # & y z y m   # $ { { ? | |   z o      ???? "0 theleadingzeros theLeadingZeros x  } ~ } l  ' '????????  ??  ??   ~   ?  l  ' '?? ? ???   ?   div is integer division    ? ? ? ? 0   d i v   i s   i n t e g e r   d i v i s i o n ?  ? ? ? r   ' 0 ? ? ? n   ' . ? ? ? 1   , .??
?? 
leng ? l  ' , ????? ? c   ' , ? ? ? l  ' * ????? ? _   ' * ? ? ? o   ' (???? 0 	thenumber 	theNumber ? m   ( )???? ??  ??   ? m   * +??
?? 
TEXT??  ??   ? o      ???? 0 thedigitcount theDigitCount ?  ? ? ? l  1 1????????  ??  ??   ?  ? ? ? r   1 8 ? ? ? \   1 6 ? ? ? l  1 4 ????? ? [   1 4 ? ? ? o   1 2???? 00 themaxleadingzerocount theMaxLeadingZeroCount ? m   2 3???? ??  ??   ? o   4 5???? 0 thedigitcount theDigitCount ? o      ???? &0 thecharactercount theCharacterCount ?  ? ? ? l  9 9????????  ??  ??   ?  ? ? ? U   9 L ? ? ? r   @ G ? ? ? c   @ E ? ? ? l  @ C ????? ? b   @ C ? ? ? o   @ A???? "0 theleadingzeros theLeadingZeros ? m   A B ? ? ? ? ?  0??  ??   ? m   C D??
?? 
TEXT ? o      ???? "0 theleadingzeros theLeadingZeros ? o   < =???? &0 thecharactercount theCharacterCount ?  ? ? ? l  M M????????  ??  ??   ?  ? ? ? l  M M?? ? ???   ? > 8 Make the number negative, if it was previously negative    ? ? ? ? p   M a k e   t h e   n u m b e r   n e g a t i v e ,   i f   i t   w a s   p r e v i o u s l y   n e g a t i v e ?  ? ? ? Z  M \ ? ????? ? =   M P ? ? ? o   M N???? 0 
isnegative 
isNegative ? m   N O??
?? boovtrue ? r   S X ? ? ? b   S V ? ? ? m   S T ? ? ? ? ?  - ? o   T U???? "0 theleadingzeros theLeadingZeros ? o      ???? "0 theleadingzeros theLeadingZeros??  ??   ?  ? ? ? l  ] ]????????  ??  ??   ?  ? ? ? l  ] ]?? ? ???   ? !  Return the prefixed number    ? ? ? ? 6   R e t u r n   t h e   p r e f i x e d   n u m b e r ?  ? ? ? L   ] e ? ? c   ] d ? ? ? l  ] b ????? ? b   ] b ? ? ? o   ] ^???? "0 theleadingzeros theLeadingZeros ? l  ^ a ????? ? c   ^ a ? ? ? o   ^ _???? 0 	thenumber 	theNumber ? m   _ `??
?? 
ctxt??  ??  ??  ??   ? m   b c?
? 
TEXT ?  ? ? ? l  f f?~?}?|?~  ?}  ?|   ?  ??{ ? l  f f?z ? ??z   ? M G If the number is greater than or equal to the maximum number of digits    ? ? ? ? ?   I f   t h e   n u m b e r   i s   g r e a t e r   t h a n   o r   e q u a l   t o   t h e   m a x i m u m   n u m b e r   o f   d i g i t s?{  ??   ^ k   j n ? ?  ? ? ? l  j j?y ? ??y   ? !  Return the original number    ? ? ? ? 6   R e t u r n   t h e   o r i g i n a l   n u m b e r ?  ??x ? L   j n ? ? c   j m ? ? ? o   j k?w?w 0 	thenumber 	theNumber ? m   k l?v
?v 
ctxt?x  ??   1  ? ? ? l     ?u?t?s?u  ?t  ?s   ?  ? ? ? l     ?r ? ??r   ? B <loop over all possible stop and shop 6-digit product numbers    ? ? ? ? x l o o p   o v e r   a l l   p o s s i b l e   s t o p   a n d   s h o p   6 - d i g i t   p r o d u c t   n u m b e r s ?  ? ? ? l    a ??q?p ? Y     a ??o ? ??n ? k   
 \ ? ?  ? ? ? r   
  ? ? ? I   
 ?m ??l?m 20 addleadingzerostonumber addLeadingZerosToNumber ?  ? ? ? o    ?k?k 0 loopvar loopVar ?  ??j ? m    ?i?i ?j  ?l   ? o      ?h?h 0 prodnum prodNum ?  ? ? ? l   ?g?f?e?g  ?f  ?e   ?  ? ? ? O    < ? ? ? k    ; ? ?  ? ? ? I   ?d?c?b
?d .miscactvnull??? ??? null?c  ?b   ?  ? ? ? l   ?a?`?_?a  ?`  ?_   ?  ?^  O    ; k   % :  r   % , b   % (	 m   % &

 ? t h t t p s : / / s t o p a n d s h o p . c o m / a p i / v 5 . 0 / p r o d u c t s / i n f o / 2 / 5 0 0 0 0 0 5 9 /	 o   & '?]?] 0 prodnum prodNum 1   ( +?\
?\ 
pURL  l  - -?[?Z?Y?[  ?Z  ?Y    l  - -?X?X    wait for page to load    ? * w a i t   f o r   p a g e   t o   l o a d  I  - 2?W?V
?W .sysodelanull??? ??? nmbr m   - .?U?U ?V    l  3 3?T?S?R?T  ?S  ?R   ?Q r   3 : l  3 8?P?O I  3 8?N?M
?N .sfridojsnull???     ctxt m   3 4 ? ? d o c u m e n t . e v a l u a t e ( ' / h t m l / b o d y / p r e / t e x t ( ) ' ,   d o c u m e n t . b o d y ,   n u l l ,   X P a t h R e s u l t . F I R S T _ O R D E R E D _ N O D E _ T Y P E ,   n u l l ) . s i n g l e N o d e V a l u e . d a t a?M  ?P  ?O   o      ?L?L 0 payload  ?Q   4    "?K
?K 
docu m     !?J?J ?^   ? m      ?                                                                                  sfri  alis    "  Macintosh HD                   BD ????
Safari.app                                                     ????            ????  
 cu             Applications  /:Applications:Safari.app/   
 S a f a r i . a p p    M a c i n t o s h   H D  Applications/Safari.app   / ??   ? !"! l  = =?I?H?G?I  ?H  ?G  " #$# Z   = Z%&?F?E% ?   = D'(' l  = B)?D?C) I  = B?B*?A
?B .corecnte****       ***** o   = >?@?@ 0 payload  ?A  ?D  ?C  ( m   B C???? P& I   G V?>+?=?> 0 write_to_file  + ,-, b   H Q./. b   H M010 m   H K22 ?33 ` / U s e r s / n o a h / D o c u m e n t s / Y a l e S e n i o r / c s 4 9 0 / p r o d u c t s /1 o   K L?<?< 0 prodnum prodNum/ m   M P44 ?55 
 . j s o n- 6?;6 o   Q R?:?: 0 payload  ?;  ?=  ?F  ?E  $ 7?97 l  [ [?8?7?6?8  ?7  ?6  ?9  ?o 0 loopvar loopVar ? m    ?5?5PV ? m    ?4?4  B??n  ?q  ?p   ? 898 l     ?3?2?1?3  ?2  ?1  9 :;: l     ?0?/?.?0  ?/  ?.  ; <=< l     ?-?,?+?-  ?,  ?+  = >?> l     ?*?)?(?*  ?)  ?(  ? @A@ l      ?'BC?'  B
tell application "Numbers"
	activate
	make new document
	
	tell document 1
		tell sheet 1
			tell table 1
				repeat with i from 1 to count of university
					set value of cell (i + 1) of column 1 to item 1 of item i of university
					set value of cell (i + 1) of column 2 to item 2 of item i of university
					set value of cell (i + 1) of column 3 to item 3 of item i of university
					set value of cell (i + 1) of column 4 to item 4 of item i of university
				end repeat
			end tell
		end tell
	end tell
end tell
   C ?DD 
 t e l l   a p p l i c a t i o n   " N u m b e r s " 
 	 a c t i v a t e 
 	 m a k e   n e w   d o c u m e n t 
 	 
 	 t e l l   d o c u m e n t   1 
 	 	 t e l l   s h e e t   1 
 	 	 	 t e l l   t a b l e   1 
 	 	 	 	 r e p e a t   w i t h   i   f r o m   1   t o   c o u n t   o f   u n i v e r s i t y 
 	 	 	 	 	 s e t   v a l u e   o f   c e l l   ( i   +   1 )   o f   c o l u m n   1   t o   i t e m   1   o f   i t e m   i   o f   u n i v e r s i t y 
 	 	 	 	 	 s e t   v a l u e   o f   c e l l   ( i   +   1 )   o f   c o l u m n   2   t o   i t e m   2   o f   i t e m   i   o f   u n i v e r s i t y 
 	 	 	 	 	 s e t   v a l u e   o f   c e l l   ( i   +   1 )   o f   c o l u m n   3   t o   i t e m   3   o f   i t e m   i   o f   u n i v e r s i t y 
 	 	 	 	 	 s e t   v a l u e   o f   c e l l   ( i   +   1 )   o f   c o l u m n   4   t o   i t e m   4   o f   i t e m   i   o f   u n i v e r s i t y 
 	 	 	 	 e n d   r e p e a t 
 	 	 	 e n d   t e l l 
 	 	 e n d   t e l l 
 	 e n d   t e l l 
 e n d   t e l l 
A EFE l     ?&?%?$?&  ?%  ?$  F GHG l     ?#?"?!?#  ?"  ?!  H IJI l      ? KL?   KMG
		set numOfSchools to do JavaScript "document.getElementsByClassName('font-md font-weight-bold d-block').length"
		
		set university to {}
		
		repeat with i from 0 to numOfSchools - 1
			set degreeType to do JavaScript "document.getElementsByClassName('font-md font-weight-bold d-block')[" & i & "].textContent"
			set universityName to do JavaScript "document.getElementsByClassName('font-sm m-b-0 d-block')[" & i & "].textContent"
			set duration to do JavaScript "document.getElementsByClassName('program-details font-weight-bold m-t-1s m-b-0')[" & i & "].textContent"
			set ranking to do JavaScript "document.getElementsByClassName('ranking-description font-sm d-block m-t-1s')[" & i & "].textContent"
			
			--append this set to university
			set end of university to {degreeType, universityName, duration, ranking}
		end repeat
		   L ?MM? 
 	 	 s e t   n u m O f S c h o o l s   t o   d o   J a v a S c r i p t   " d o c u m e n t . g e t E l e m e n t s B y C l a s s N a m e ( ' f o n t - m d   f o n t - w e i g h t - b o l d   d - b l o c k ' ) . l e n g t h " 
 	 	 
 	 	 s e t   u n i v e r s i t y   t o   { } 
 	 	 
 	 	 r e p e a t   w i t h   i   f r o m   0   t o   n u m O f S c h o o l s   -   1 
 	 	 	 s e t   d e g r e e T y p e   t o   d o   J a v a S c r i p t   " d o c u m e n t . g e t E l e m e n t s B y C l a s s N a m e ( ' f o n t - m d   f o n t - w e i g h t - b o l d   d - b l o c k ' ) [ "   &   i   &   " ] . t e x t C o n t e n t " 
 	 	 	 s e t   u n i v e r s i t y N a m e   t o   d o   J a v a S c r i p t   " d o c u m e n t . g e t E l e m e n t s B y C l a s s N a m e ( ' f o n t - s m   m - b - 0   d - b l o c k ' ) [ "   &   i   &   " ] . t e x t C o n t e n t " 
 	 	 	 s e t   d u r a t i o n   t o   d o   J a v a S c r i p t   " d o c u m e n t . g e t E l e m e n t s B y C l a s s N a m e ( ' p r o g r a m - d e t a i l s   f o n t - w e i g h t - b o l d   m - t - 1 s   m - b - 0 ' ) [ "   &   i   &   " ] . t e x t C o n t e n t " 
 	 	 	 s e t   r a n k i n g   t o   d o   J a v a S c r i p t   " d o c u m e n t . g e t E l e m e n t s B y C l a s s N a m e ( ' r a n k i n g - d e s c r i p t i o n   f o n t - s m   d - b l o c k   m - t - 1 s ' ) [ "   &   i   &   " ] . t e x t C o n t e n t " 
 	 	 	 
 	 	 	 - - a p p e n d   t h i s   s e t   t o   u n i v e r s i t y 
 	 	 	 s e t   e n d   o f   u n i v e r s i t y   t o   { d e g r e e T y p e ,   u n i v e r s i t y N a m e ,   d u r a t i o n ,   r a n k i n g } 
 	 	 e n d   r e p e a t 
 	 	J NON l     ????  ?  ?  O P?P l      ?QR?  Q ? ?
	tell document 1
		set URL to "https://stopandshop.com/product-search"
		delay 10
		
		log (do JavaScript "document.querySelectorAll('[id^=product-name-]').length")
	end tell
	   R ?SSb 
 	 t e l l   d o c u m e n t   1 
 	 	 s e t   U R L   t o   " h t t p s : / / s t o p a n d s h o p . c o m / p r o d u c t - s e a r c h " 
 	 	 d e l a y   1 0 
 	 	 
 	 	 l o g   ( d o   J a v a S c r i p t   " d o c u m e n t . q u e r y S e l e c t o r A l l ( ' [ i d ^ = p r o d u c t - n a m e - ] ' ) . l e n g t h " ) 
 	 e n d   t e l l 
 	?       ?TUVWX?  T ????
? 
pimr? 0 write_to_file  ? 20 addleadingzerostonumber addLeadingZerosToNumber
? .aevtoappnull  ?   ? ****U ?Y? Y  Z[Z ? ?
? 
vers?  [ ?\?
? 
cobj\ ]]   ?
? 
osax?  V ? ??^_?? 0 write_to_file  ? ?`? `  ?
?	?
 0 target_file  ?	 0 data_to_write  ?  ^ ???? 0 target_file  ? 0 data_to_write  ? 0 myfile myFile_ ?????
? 
perm
? .rdwropenshor       file
? 
refn
? .rdwrwritnull???     ****
? .rdwrclosnull???     ****? ??el E?O???l O?j W ?  3????ab???  20 addleadingzerostonumber addLeadingZerosToNumber?? ??c?? c  ?????? 0 	thenumber 	theNumber?? 00 themaxleadingzerocount theMaxLeadingZeroCount??  a ???????????????? 0 	thenumber 	theNumber?? 00 themaxleadingzerocount theMaxLeadingZeroCount?? 0 
isnegative 
isNegative?? 0 thethreshold theThreshold?? "0 theleadingzeros theLeadingZeros?? 0 thedigitcount theDigitCount?? &0 thecharactercount theCharacterCountb ???? {???? ? ????? 

?? 
long
?? 
TEXT
?? 
leng
?? 
ctxt?? o?jE?O??$?&E?O?? X?e  	?'E?Y hO?E?O?k"?&?,E?O?k?E?O ?kh??%?&E?[OY??O?e  
??%E?Y hO???&%?&OPY ??&X ??d????ef??
?? .aevtoappnull  ?   ? ****d k     agg  ?????  ??  ??  e ???? 0 loopvar loopVarf ?????????? ????
????????????24????PV??  B??? ?? 20 addleadingzerostonumber addLeadingZerosToNumber?? 0 prodnum prodNum
?? .miscactvnull??? ??? null
?? 
docu
?? 
pURL
?? .sysodelanull??? ??? nmbr
?? .sfridojsnull???     ctxt?? 0 payload  
?? .corecnte****       ****?? P?? 0 write_to_file  ?? b `??kh  *??l+ E?O? %*j O*?k/ ??%*?,FOmj 
O?j E?UUO?j ? *a ?%a %?l+ Y hOP[OY??ascr  ??ޭ