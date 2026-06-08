import os
for root, dirs, files in os.walk('/app/hibernate-validator'):
    for file in files:
        if 'PropertyPathImpl' in file or 'NodeImpl' in file or 'RecordComponent' in file:
            print(os.path.join(root, file))
