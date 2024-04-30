from flask import Flask
from flask import redirect, url_for
from voltdbclient import *
import urllib.request, json

app = Flask(__name__)

@app.route('/', methods=['GET'])
def index():
    return redirect(url_for('static', filename='index.html'))

@app.route('/busy')
def busy():   
    with urllib.request.urlopen("http://localhost:8080/api/2.0/?Procedure=get_busy_stations") as url:
        data = json.load(url)
        return data
#    client = FastSerializer("localhost", 21212)
#    proc = VoltProcedure( client, "get_busy_stations")
#    response = proc.call()
#    for x in response.tables:
#        print(x)

if __name__ == '__main__':
    app.run(debug=False, port=8081)
