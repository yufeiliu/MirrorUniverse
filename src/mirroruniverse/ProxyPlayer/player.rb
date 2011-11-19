# Ruby implementation of RandomPlayer
# Interfaces with ProxyPlayer to communicate via STDIN/STDOUT
# Yufei Liu

STDOUT.sync = true

DIR = [[4,3,2], [5,0,1], [6,7,8]]
DIR_TO_MAP = [[ 0, 0 ], [ 1, 0 ], [ 1, -1 ], [ 0, -1 ], [ -1, -1 ],  [ -1, 0 ], [ -1, 1 ], [ 0, 1 ], [ 1, 1 ] ];

left_over=false
right_over=false

def play(left_view, right_view)
  local_view_left = []
  local_view_right = []

  for index in (0..2)
    local_view_left[index]=[]
    local_view_right[index]=[]
  end

  mid = left_view.length / 2
  for i in (-1..1)
    for j in (-1..1)
      local_view_left[j+1][i+1] = left_view[mid+j][mid+i]
      if (local_view_left[j+1][i+1] == 2)
        if (i==0 && j==0)
          next
        else
          left_over=true
          return DIR[j+1][i+1]
        end
      end
    end
  end

  mid = right_view.length / 2
  for i in (-1..1)
    for j in (-1..1)
      local_view_right[j+1][i+1] = right_view[mid+j][mid+i]
      if (local_view_right[j+1][i+1] == 2)
        if (i==0 && j==0)
          next
        else
          right_over=true
          return DIR[j+1][i+1]
        end
      end
    end
  end


  start_once = true
  y=-1
  x=-1
  d=0

  if (!left_over)
    # puts "left not over yet"
    while (start_once || local_view_left[y+1][x+1] == 1)
      start_once = false
      d = rand(8)+1
      x = DIR_TO_MAP[d][0]
      y = DIR_TO_MAP[d][1]
    end
  else
    while (start_once || local_view_right[y+1][x+1] == 1)
      start_once = false
      d = rand(8)+1
      x = DIR_TO_MAP[d][0]
      y = DIR_TO_MAP[d][1]
    end
  end

  return d
end

while input=gets.chomp
  params = input.split(';')
  lview = eval(params[0])
  rview = eval(params[1])
  puts play(lview, rview)
end