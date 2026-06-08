import subprocess
import sys

try:
    result = subprocess.run(['mvn', 'test', '-Dtest=RecordConstrainedTest', '-pl', 'engine'], 
                            cwd='/app/hibernate-validator', capture_output=True, text=True)
    print("STDOUT:", result.stdout)
    print("STDERR:", result.stderr)
    print("RETURN CODE:", result.returncode)
except Exception as e:
    print("Error:", e)
