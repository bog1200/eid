VBoxManage registervm "C:\ProgramData\Multipass\data\virtualbox\vault\instances\orange-vm\orange-vm.vbox"
VBoxManage modifyvm orange-vm --uart1 off
VBoxManage modifyvm orange-vm --nictype2 virtio --nic2 hostonly --hostonlyadapter2 "VirtualBox Host-Only Ethernet Adapter"
