#!/bin/bash

# Get sorted lists of input and output folders
input_folders=(networkFiles/keys/0x*/)
output_folders=(nodes/Node-*/data/)

# Check if input and output folder counts match
if [ "${#input_folders[@]}" -ne "${#output_folders[@]}" ]; then
  echo "Error: Mismatch in number of input and output folders."
  echo "Inputs: ${#input_folders[@]}, Outputs: ${#output_folders[@]}"
  exit 1
fi

# Copy key folders
for i in "${!input_folders[@]}"; do
  input="${input_folders[$i]}"
  output="${output_folders[$i]}"

  echo "Copying keys from $input to $output"
  cp -r "$input"* "$output"
done

# Copy genesis.json to each Node-* directory
for node_dir in Node-*; do
  echo "Copying genesis.json to $node_dir/"
  cp networkFiles/genesis.json "$node_dir/"
done

echo "✅ All keys and genesis files copied."
