#!/usr/bin/env bash

if [ "$0" = sh -o "$0" = /bin/sh -o "$0" = /usr/bin/sh -o "$0" = bash -o "$0" = -bash -o "$0" = /bin/bash -o "$0" = /usr/bin/bash ]; then
  echo 'Please do _not_ source this file!'
  return 1
fi

echo
for countdown in {9..1}; do
  echo Are you sure you want to run the server in strict mode?
  echo Press Control-C [or do something else similar to that] if not...  counting down, $countdown ...
  echo
  sleep 1s
done

ant && java -jar ./Build/jars/Configuration_Master.jar strict_checking verbosity=9 "$@"
result="$?"
/usr/bin/env echo -e "\nResult code: $result"
exit $result
