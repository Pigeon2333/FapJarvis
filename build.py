# -*- coding: utf-8 -*-
"""编译+打包 FapJarvis（javac, 仅依赖 server.jar）"""
import subprocess, os, glob, zipfile

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, 'src', 'main', 'java')
RESOURCES = os.path.join(ROOT, 'src', 'main', 'resources')
OUT = os.path.join(ROOT, 'build_classes')

SERVER_JAR = r'E:\FAPIXEL小游戏中国版\服务端\server.jar'
FAPSOUND_JAR = r'E:\FAPIXEL小游戏中国版\插件\FapSound-1.0.0.jar'
cp = f'{SERVER_JAR};{FAPSOUND_JAR}'

java_files = glob.glob(os.path.join(SRC, '**', '*.java'), recursive=True)
print(f'Found {len(java_files)} Java source files')

# Clean
if os.path.exists(OUT):
    import shutil; shutil.rmtree(OUT)
os.makedirs(OUT)

# Compile
cmd = ['javac', '-source', '17', '-target', '17', '-encoding', 'UTF-8', '-cp', cp, '-d', OUT] + java_files
print('Compiling...')
result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
if result.returncode != 0:
    print('STDERR:', result.stderr)
    print('STDOUT:', result.stdout)
    raise SystemExit(1)
print('Compilation successful!')

# Read version
import re
with open(os.path.join(RESOURCES, 'plugin.yml'), 'r', encoding='utf-8') as f:
    m = re.search(r'version:\s*"?([^"\n]+)"?', f.read())
    VERSION = m.group(1).strip() if m else 'unknown'
print(f'Plugin version: {VERSION}')

# Package
output_jar = os.path.join(ROOT, f'FapJarvis-{VERSION}.jar')
with zipfile.ZipFile(output_jar, 'w', zipfile.ZIP_DEFLATED) as zout:
    for f in glob.glob(os.path.join(OUT, '**', '*'), recursive=True):
        if os.path.isfile(f):
            arcname = os.path.relpath(f, OUT).replace('\\', '/')
            zout.write(f, arcname)
    for f in glob.glob(os.path.join(RESOURCES, '**', '*'), recursive=True):
        if os.path.isfile(f):
            arcname = os.path.relpath(f, RESOURCES).replace('\\', '/')
            zout.write(f, arcname)

size_kb = os.path.getsize(output_jar) / 1024
print(f'Jar written: {output_jar} ({size_kb:.1f} KB)')
