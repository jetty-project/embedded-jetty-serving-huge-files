#!/bin/bash

WORKDIR=$TMP/jetty-huge-work

if [[ ! -d ${WORKDIR} ]] ; then
  echo "ERROR: Work Dir doesn't exist.  Have you started the server yet?"
  exit 1
fi

dd if=/dev/urandom of=${WORKDIR}/test.txt bs=1048576 count=1
dd if=/dev/urandom of=${WORKDIR}/file4G.txt bs=1048576 count=4000
dd if=/dev/urandom of=${WORKDIR}/file10G.txt bs=1048576 count=10000
