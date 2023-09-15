#!/bin/bash

# Loop through each line in the TSV file and download the file in the second column
while IFS=$'\t' read -r name url; do
    # Check if the line is not empty and contains a valid URL
    if [[ ! -z "$url" && "$url" =~ ^http(s)?:// ]]; then
        # Use wget to download the file to the specified directory
        wget -P "data/ontologies" "$url"
    else
        echo "Invalid URL: $url"
    fi
done < "data/download_links.tsv"

echo "Download completed."

