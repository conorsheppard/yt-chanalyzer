#!/bin/bash

mkdir -p $HOME/.venvs
python3 -m venv $HOME/.venvs/MyEnv
source $HOME/.venvs/MyEnv/bin/activate

pip install requests
pip install typing_extensions

java -jar /chanalyzer.jar