#!/usr/bin/env bash

if [ "$0" = sh -o "$0" = /bin/sh -o "$0" = /usr/bin/sh -o "$0" = bash -o "$0" = -bash -o "$0" = /bin/bash -o "$0" = /usr/bin/bash ]; then
  echo 'Please do _not_ source this file!'
  return 1
fi

dirs_to_try=`find Tests/static-only/expected_to_pass/* -type d -maxdepth 0`
echo 'Subdir.s to try as tests:'
for dir in $dirs_to_try; do
  echo "  $dir"
done
echo

declare -i num_passed=0
declare -i num_failed=0

for dir in $dirs_to_try; do
  echo "  $dir"
done

