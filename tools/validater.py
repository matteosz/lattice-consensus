import os
import subprocess

dir_path = "../logs/"
files = os.listdir(dir_path)
new_files = []

for f in files:
    if f.endswith('.output'):
        new_files.append(dir_path + f)


to_run = ["python3", "validate_fifo.py", "--proc_num", str(len(new_files))] + new_files
subprocess.run(to_run)
