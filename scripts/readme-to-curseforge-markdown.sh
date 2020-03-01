#!/usr/bin/env sh

cat README.adoc \
| perl -0777 -pe 's/\n\.([^\n]*)\n(image::[^\n]*)\n/\n\2\n_\1_\n/g' \
| asciidoctor -b docbook -a leveloffset=+1 -a imagesdir="https://github.com/MaienM/AccessibilityMod/raw/master/" -o - - \
| pandoc --atx-headers --wrap=preserve -t markdown_strict -f docbook - \
| perl -0777 -pe 's/\n(!\[[^\n]*)\n\n/\n\1  \n/g'
