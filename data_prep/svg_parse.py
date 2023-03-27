# converts a list of path elements of a SVG file to simple line drawing commands
from svg.path import parse_path
from svg.path.path import Line
from xml.dom import minidom

# read the SVG file
doc = minidom.parse('/home/nodog/Downloads/svg_ss.svg')
path_strings = [path.getAttribute('d') for path in doc.getElementsByTagName('path')]


rects = doc.getElementsByTagName('rect')




#print the line draw commands
for path_string in path_strings:
    path = parse_path(path_string)

    for e in path:
        if isinstance(e, Line):
            x0 = e.start.real
            y0 = e.start.imag
            x1 = e.end.real
            y1 = e.end.imag

            print("Line from (%.2f, %.2f) to (%.2f, %.2f)" % (x0, y0, x1, y1))

ctr = 0
for rect in rects:
    rot = 0
    trans = rect.getAttribute('transform')
    if (trans != ''):
        if (trans[:6] == 'rotate'):
            rot = float(trans.split("(", 1)[1].split(" ", 1)[0])

    x = float(rect.getAttribute('x'))
    y = float(rect.getAttribute('y'))
    ht = float(rect.getAttribute('height'))
    width = float(rect.getAttribute('width'))

    left = x
    top = y
    right = left + width
    bottom = top + ht


    print("rectList.add(new StoreElement(new RectF(%.2ff, %.2ff, %.2ff, %.2ff), %.2ff));" % (left, top, right, bottom, rot))
    #print("rectList.add(new RectF(%.2ff, %.2ff, %.2ff, %.2ff));" % (left, top, right, bottom))
    ctr += 1


doc.unlink()

