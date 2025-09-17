#!/bin/bash
SESSION="workers"

# Create session with first pane (Java Worker)
tmux new-session -d -s $SESSION

# Split into 5 panes
# Split horizontally to create top and bottom
tmux split-window -v

# Split top pane vertically twice (3 panes on top)
tmux select-pane -t 0
tmux split-window -h
tmux split-window -h

# Split bottom pane vertically once (2 panes on bottom)
tmux select-pane -t 3
tmux split-window -h

# Run commands in each pane
# Pane 0: Java Worker
tmux send-keys -t 0 'cd java && ./startlocalworker.sh' Enter

# Pane 1: Golang Worker  
tmux send-keys -t 1 'cd go && ./startlocalworker.sh' Enter

# Pane 2: .NET Worker
tmux send-keys -t 2 'cd dotnet && ./startlocalworker.sh' Enter

# Pane 3: Python Worker
tmux send-keys -t 3 'cd python && poetry install && ./startlocalworker.sh' Enter

# Pane 4: TypeScript Worker
tmux send-keys -t 4 'cd typescript && npm install && ./startlocalworker.sh' Enter

# Attach to session
tmux attach -t $SESSION