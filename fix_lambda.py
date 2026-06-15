import re

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'r', encoding='utf-8') as f:
    code = f.read()

# Fix showFacilityMenu lambdas
# Replace (BurningStation)object3 with (BurningStation)building
code = code.replace('(BurningStation)object3', '(BurningStation)building')
# Replace (Barracks)object3 with (Barracks)building
code = code.replace('(Barracks)object3', '(Barracks)building')
# Replace (VehicleFactory)object3 with (VehicleFactory)building
code = code.replace('(VehicleFactory)object3', '(VehicleFactory)building')

# Fix units.remove(unit2)
# unit2 is from: unit2 = HexGrid.this.getUnitAt(n2, n3);
# let's just make it final
code = code.replace('unit2 = HexGrid.this.getUnitAt(n2, n3);', 'final Unit f_unit2 = HexGrid.this.getUnitAt(n2, n3);\n                        unit2 = f_unit2;')
code = code.replace('HexGrid.this.units.remove(unit2);', 'HexGrid.this.units.remove(f_unit2);')
code = code.replace('if (unit2 instanceof NukeCarrier', 'if (f_unit2 instanceof NukeCarrier')

with open(r"C:\prj\hexgame\src\main\java\com\hexgame\HexGrid.java", 'w', encoding='utf-8') as f:
    f.write(code)

print("Lambda fix applied")
