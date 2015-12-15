# Set terminal & output
set terminal postscript eps enhanced color solid font 'Helvetica,25'

# Output
set output outputfile
set datafile separator ','

# Axis
set ylabel "Throughput (txn/s)"
set xlabel "Clients"
set xtics nomirror
set ytics nomirror
#set xrange [0:100000]
#set yrange [0:*]
#set xtics nomirror rotate by -30 scale 0.5
#set xtics font "Helvetica,16"

# Key
set key top left

# Grid
set style line 12 lc rgb '#808080' lt 0 lw 1
set grid ytics back ls 12

#RGB Colors
red = '#a2142f'
yellow='#edb120'
green='#77ac30' # green
purple='#7e2f8e' # purple
orange='#d95319' # orange
blue = '#0056bd'

# Plot type
set style data histogram
set style histogram cluster gap 1

#every A:B:C:D:E:F
#A: line increment
#B: data block increment
#C: The first line
#D: The first data block
#E: The last line
#F: The last data block

plot data1 every 18::4 using ($3/60):xtic(1) lc rgb blue lw 3.5 fs pattern 1 title 'Unique IDs', data2 every 18::4 using ($3/60):xtic(1) lc rgb orange lw 3.5 fs pattern 6 title 'Unique+Seq IDs'






