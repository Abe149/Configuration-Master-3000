#!/usr/bin/env bash

if [ "$0" = sh -o "$0" = /bin/sh -o "$0" = /usr/bin/sh -o "$0" = bash -o "$0" = -bash -o "$0" = /bin/bash -o "$0" = /usr/bin/bash ]; then
  echo 'Please do _not_ source this file!'
  return 1
fi

pushd `dirname "$0"` > /dev/null
ant && java -jar ./Build/jars/Configuration_Master.jar "$@"
result="$?"
/usr/bin/env echo -e "\nResult code: $result"
popd > /dev/null
exit $result
