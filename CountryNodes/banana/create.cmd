@echo off
multipass launch -n banana-vm --cloud-init banana-user-data.yml --disk 10G -vvvv 