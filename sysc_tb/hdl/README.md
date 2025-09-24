# `hdl` directory

You can either add `.v/.hdlinfo.json` files directly to this folder, or use symbolic links.

Here is the command for generating symbolic links (replace `NAMESPACE` with the Scala package decleration, like `chext_examples`):
```bash
ln -s ../../chisel_rtl/sysc_tb/NAMESPACE/hdl/ NAMESPACE
```
