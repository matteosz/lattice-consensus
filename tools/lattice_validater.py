import sys 

if(len(sys.argv) != 5):
    print("Incorrect arguments")
    print()
    print("Tool usage: python3 lattice_validater.py <config-path> <output-path> <number-of-processes> <number-of-proposal>")
    print("The config format should be lattice-agreement-<id>.config")
    print("The output format should be <id>.output")
    
    exit()

config_path = sys.argv[1]
if config_path[-1] != '/':
    config_path += '/'

output_path = sys.argv[2]
if output_path[-1] != '/':
    output_path += '/'

number_processes = int(sys.argv[3])
number_proposal = int(sys.argv[4])

proposes = []
for i in range(number_proposal):
    proposes.append([])

for i in range(1, number_processes+1):
    try:
        with open(config_path + 'lattice-agreement-' + str(i) + '.config') as f:
            lines = f.readlines()
            lines = lines[1:]
            
            p = 0
            for l in lines:
                num_set = set()
                new_l = l.strip().split(' ')
                for n in new_l:
                    num_set.add(int(n))
                proposes[p].append(num_set)
                p+=1
    except:
        print("File not found: " + config_path + 'lattice-agreement-' + str(i) + '.config')


decisions = []
for i in range(number_proposal):
    decisions.append([])

for i in range(1, number_processes+1):
    try:
        with open(output_path + str(i) + '.output') as f:
            lines = f.readlines()

            p=0
            for l in lines:
                num_set = set()
                new_l = l.strip().split(' ')
                for n in new_l:
                    num_set.add(int(n))
                decisions[p].append(num_set)
                p+=1

            for j in range(p, number_proposal):
                decisions[j].append(set())

    except EnvironmentError:
        print(EnvironmentError)
        print("File not found: " + output_path + str(i) + '.output')

error = False
# Validity
for i in range(number_proposal):
    
    all_proposed = set()
    for j in proposes[i]:
        for elem in j:
            all_proposed.add(elem)
    
    for j in range(len(decisions[i])):

        # Not decided yet
        if len(decisions[i][j]) == 0:
            continue

        # I_i subset of O_i
        if not (proposes[i][j].issubset(decisions[i][j])):
            print(f"NOT CONTAINING PROPOSED: Validation failed for proposal {i} of process {j}")
            print("Proposed_i - decided_i: ", proposes[i][j] - decisions[i][j])
            error = True
            exit()

        # O_i subset of all proposed
        if not (decisions[i][j].issubset(all_proposed)):
            print(f"OUT OF PROPOSED: Validation failed for proposal {i} of process {j}")
            print("Decided_i - all_proposed: ", decisions[i][j] - all_proposed)
            error = True
            exit()

        # Consistency
        for k in range(j+1, len(decisions[i])):
            # O_j subset of O_k
            if not (decisions[i][j].issubset(decisions[i][k]) or decisions[i][k].issubset(decisions[i][j])):
                print(f"CONSISTENCY: Validation failed for proposal {i} of process {j}")
                print("Decided_i: ", decisions[i][j])
                print(f"Not consistent with what decided by process {k}: ", decisions[i][k])
                error = True
                exit()

if not error:
    print("Validation successful!")
         
